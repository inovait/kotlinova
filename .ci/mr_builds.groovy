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
