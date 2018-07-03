import java.util.regex.Pattern

String changelog = null
String newVersion = null





node('build-ubuntu') {
    deleteDir()

    sshagent(['388b013e-c31a-4a8c-aad6-bb06aff2a513']) {
        sh "git clone git@hydra:matejd/kotlinova.git"
    }

    dir("kotlinova") {
        env.GIT_BRANCH = "master"
        sh "git status"
        sh "npx semantic-release"

        if (!fileExists("next_version.txt")) {
            return
        }

        newVersion = readFile("next_version.txt")
        changelog = readFile("next_changelog.md")
    }
}

node('android') {
    if (newVersion == null) {
        println("Release not created. Aborting")
        return
    }

    stage('Start') {
        git(branch: 'master',
                credentialsId: '388b013e-c31a-4a8c-aad6-bb06aff2a513',
                url: 'git@hydra:matejd/kotlinova.git')
    }

    stage('Update Version') {
        def versionSplit = newVersion.split(Pattern.quote("."))

        def versionProperties = "MAJOR=${versionSplit[0]}\nMINOR=${versionSplit[1]}\nPATCH=${versionSplit[2]}" as String
        writeFile file: 'version.properties', text: versionProperties
    }

    try {
        stage('Build app') {
            bat 'gradlew clean assemble'
        }
        stage('Lint') {
            bat 'gradlew ktlint lintDebug'
        }
        stage('Build tests') {
            bat 'gradlew compileDebugUnitTestSources assembleAndroidTest ' +
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
        stage('Publish') {
            bat 'gradlew uploadArchives'
        }
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

    stage('Update git') {
        changelog = changelog.replaceAll("https:/./", "http://hydra/matejd/kotlinova/")

        def existingChangelog = ""

        if (fileExists("CHANGELOG.MD")) {
            existingChangelog = "\n" + readFile("CHANGELOG.MD")
        }

        def updatedChangelog = changelog + existingChangelog
        writeFile file: 'CHANGELOG.MD', text: updatedChangelog

        sh "git add version.properties"
        sh "git add CHANGELOG.MD"

        sh "git commit -m \"chore: release $newVersion\n[ci-skip]\" --author=\"Jenkins <hudson@inova.si>\""

        sh "git tag v$newVersion"

        sshagent(['388b013e-c31a-4a8c-aad6-bb06aff2a513']) {
            sh "git push origin master"
            sh "git push origin v$newVersion"
        }
    }
}