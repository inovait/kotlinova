def abortPreviousRunningBuilds() {
    def hi = Hudson.instance
    def projectName = env.JOB_NAME.split('/')[0]

    hi.getItem(projectName).getItem(env.JOB_BASE_NAME).getBuilds().each { build ->
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
            updateGitlabCommitStatus name: 'jenkins', state: 'pending'
        }
    }

    node('android') {
        try {
            stage('Start') {
                checkout scm
                updateGitlabCommitStatus name: 'jenkins', state: 'running'
            }
            stage('Build app') {
                bat 'gradlew clean assemble'
            }
            stage('Lint') {
                bat 'gradlew ktlint lintDebug'
            }
            stage('Build tests') {
                bat 'gradlew compileDebugUnitTestSources compileDebugAndroidTestSources ' +
                        'compileTestJava compileTestKotlin'
            }
            stage('Test') {
                bat 'gradlew test'
            }
            stage('Emulator Test') {
                parallel(
                        launchEmulator: {
                            // Emulator will be terminated non-successfully eventually
                            // Ignore termination error
                            try {
                                bat 'C:\\Android\\sdk\\emulator\\emulator.exe' +
                                        ' -avd Nexus_5_API_23 -no-snapshot-load -no-snapshot-save' +
                                        ' -no-window'
                            } catch (ignored) {
                            }
                        },
                        tests: {
                            try {
                                timeout(time: 100, unit: 'SECONDS') {
                                    bat('C:\\Android\\sdk\\platform-tools\\adb.exe wait-for-device')

                                    def bootFinished = "0"
                                    while (bootFinished != "1") {
                                        bootFinished = bat(script: '@C:\\Android\\sdk\\platform-tools' +
                                                '\\adb.exe shell getprop sys.boot_completed',
                                                returnStdout: true).trim()
                                        sleep(time: 1, unit: 'SECONDS')
                                    }
                                }

                                bat 'gradlew connectedDebugAndroidTest'

                                timeout(time: 10, unit: 'SECONDS') {
                                    bat "C:\\Android\\sdk\\platform-tools\\adb.exe shell reboot -p"
                                    sleep(time: 2, unit: 'SECONDS')
                                }
                            } finally {
                                try {
                                    bat('taskkill /IM emulator.exe /F')
                                } catch (ignored) {
                                    // Emulator kill is there just in case. Ignore all exceptions from it
                                }
                            }
                        })
            }
            stage('Calculate coverage') {
                jacoco classPattern: '**/classes, **/kotlin-classes/debug', exclusionPattern: '**/R.class, **/R$*.class, **/BuildConfig.*, **/Manifest*.*, **/*Test*.*, android/**/*.*'
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
