/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

//noinspection GroovyUnusedAssignment
@Library(value = 'Inova Commons', changelog = false) _

def abortPreviousRunningBuilds() {
    def hi = Jenkins.getInstanceOrNull() as Hudson

    hi.getItemByFullName(env.JOB_NAME).getBuilds().each { build ->
        def exec = build.getExecutor()

        if (build.number < currentBuild.number && exec != null) {
            exec.interrupt(
                    Result.ABORTED,
                    new CauseOfInterruption.UserInterruption(
                            "Aborted by #${currentBuild.number}"
                    )
            )
            println("Aborted previous running build #${build.number}")
        }
    }
}

properties([
        gitLabConnection('Hydra@Git')
])

try {
    node {
        stage('Prepare') {
            abortPreviousRunningBuilds()
            checkout scm
            updateGitlabCommitStatus name: 'jenkins', state: 'pending'
        }
    }

    node('android-linux') {
        try {
            stage('Start') {
                checkout scm
                updateGitlabCommitStatus name: 'jenkins', state: 'running'
            }
            stage('Build app') {
                sh './gradlew clean assemble'
            }
            stage('Lint') {
                sh './gradlew ktlint lintDebug'
            }
            stage('Build tests') {
                sh './gradlew compileDebugUnitTestSources assembleAndroidTest ' +
                        'compileTestJava compileTestKotlin'
            }
            stage('Test') {
                sh './gradlew test'
            }
            stage('Emulator Test') {
                android.withEmulator {
                    sh './gradlew connectedDebugAndroidTest'
                }
            }
            stage('Calculate coverage') {

                jacoco classPattern: '**/classes, **/kotlin-classes/debug',
                        exclusionPattern: '**/R.class, **/R$*.class, **/BuildConfig.*, **/Manifest*.*, **/*Test*.*, android/**/*.*',
                        sourceInclusionPattern: '**/*.java, **/*.kt',
                        sourceExclusionPattern: '',
                        execPattern: '**/*.exec **/*.ex'
            }
            updateGitlabCommitStatus name: 'jenkins', state: 'success'
        } finally {
            androidLint()
            checkstyle canComputeNew: false, defaultEncoding: '',
                    healthy: '', pattern: '**/build/ktlint.xml', unHealthy: ''
            openTasks canComputeNew: false, defaultEncoding: '',
                    excludePattern: '', healthy: '', high: 'WTF,FIXME',
                    low: 'println\\(,Timber\\.d,Timber\\.v', normal: 'TODO',
                    pattern: '**/*.java, **/*.kt, **/*.xml, **/*.gradle', unHealthy: ''
            warnings canComputeNew: false, canResolveRelativePaths: false,
                    categoriesPattern: '', consoleParsers: [[parserName: 'Java Compiler (javac)']],
                    defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '',
                    messagesPattern: '', unHealthy: ''
            try {
                junit testResults: '**/build/test-results/**/*.xml, ' +
                        '**/build/outputs/**/connected/*.xml'
            } catch (ignored) {
                // JUnit throws error every time it does not detect new tests, loosing real reason
                // for build to fail
            }
        }
    }
} catch (Exception e) {
    if (e instanceof InterruptedException || (e.message != null && e.message.contains("task was cancelled"))) {
        updateGitlabCommitStatus name: 'jenkins', state: 'canceled'
    } else {
        updateGitlabCommitStatus name: 'jenkins', state: 'failed'
        currentBuild.result = 'FAILURE'
    }

    def sw = new StringWriter()
    def pw = new PrintWriter(sw)
    e.printStackTrace(pw)
    echo(sw.toString())

    throw e
}
