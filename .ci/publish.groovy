import java.util.regex.Pattern

//noinspection GroovyUnusedAssignment
@Library(value = 'Inova Commons', changelog = false) _

node('android-linux') {
    stage('Git pull') {
        git(branch: 'master',
                credentialsId: '388b013e-c31a-4a8c-aad6-bb06aff2a513',
                url: 'git@hydra:utilities/kotlinova.git')
    }

    def curVersion = gitParsing.getLastVersion()
    if (curVersion == null) {
        throw IllegalArgumentException("Git does not contain version tags")
    }

    def curVersionName = "${curVersion[0]}.${curVersion[1]}.${curVersion[2]}"

    def commits = gitParsing.getCommits("v$curVersionName")
    def releaseType = releases.getReleaseType(commits)
    if (releaseType == 0) {
        println("No new tickets to release. Aborting.")
        return
    }

    def newVersion = curVersion.collect()
    if (releaseType == 1) {
        newVersion[2]++
    } else if (releaseType == 2) {
        newVersion[1]++
        newVersion[2] = 0
    } else if (releaseType == 3) {
        newVersion[0]++
        newVersion[1] = 0
        newVersion[2] = 0
    } else {
        throw IllegalStateException("Unknown release type: $releaseType")
    }

    def newVersionName = "${newVersion[0]}.${newVersion[1]}.${newVersion[2]}"

    def changelog = releases.generateChangelog(
            commits,
            curVersionName,
            newVersionName,
            "http://hydra/utilities/kotlinova/")

    stage('Update Version') {
        def versionProperties = "MAJOR=${newVersion[0]}\n" +
                "MINOR=${newVersion[1]}\n" +
                "PATCH=${newVersion[2]}" as String
        writeFile file: 'version.properties', text: versionProperties
    }

    try {
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
        stage('Publish') {
            withCredentials([
                    file(credentialsId: '6d304cc6-c5cd-40c5-8c80-92f86d620d97',
                            variable: 'MAVEN_PUBLISH_SETTINGS')
            ]) {
                sh './gradlew uploadArchives'
            }
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
        def existingChangelog = ""

        if (fileExists("CHANGELOG.MD")) {
            existingChangelog = "\n\n" + readFile("CHANGELOG.MD")
        }

        def updatedChangelog = changelog + existingChangelog
        writeFile file: 'CHANGELOG.MD', text: updatedChangelog

        sh "git add version.properties"
        sh "git add CHANGELOG.MD"

        sh "git commit -m \"release: publish $newVersionName\n[ci-skip]\" --author=\"Jenkins <hudson@inova.si>\""
        sh "git tag v$newVersionName"

        sshagent(['388b013e-c31a-4a8c-aad6-bb06aff2a513']) {
            sh "git push origin master"
            sh "git push origin v$newVersionName"
        }
    }
}