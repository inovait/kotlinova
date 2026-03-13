/*
 * Copyright 2026 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jacoco

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.setupJacocoMergingMultiplatform() {
   registerJacocoConfigurations()

   afterEvaluate {
      artifacts {
         the<KotlinMultiplatformExtension>().sourceSets.forEach { set ->
            add(CONFIGURATION_JACOCO_SOURCES, layout.projectDirectory.dir("src/${set.name}/kotlin"))
         }

         if (name.startsWith("navigation")) {
            // Navigation is the only module with the instrumented tests. For coverage to work on those, we must use Android
            // processed classes from the tmp folder
            // otherwise, we use normal JVM generated classes
            add(CONFIGURATION_JACOCO_CLASSES, layout.buildDirectory.dir("tmp/kotlin-classes/debug").map { it.asFile })
         } else {
            add(CONFIGURATION_JACOCO_CLASSES, layout.buildDirectory.dir("classes/kotlin/jvm/main").map { it.asFile })
         }
         add(CONFIGURATION_JACOCO_EXEC, layout.buildDirectory.dir("outputs/unit_test_code_coverage").map { it.asFile })
         add(CONFIGURATION_JACOCO_EXEC, layout.buildDirectory.dir("outputs/code_coverage").map { it.asFile })
         add(CONFIGURATION_JACOCO_EXEC, layout.buildDirectory.dir("jacoco").map { it.asFile })
      }
   }
}

fun Project.setupJacocoMergingAndroid() {
   registerJacocoConfigurations()

   artifacts {
      add(CONFIGURATION_JACOCO_SOURCES, layout.projectDirectory.dir("src/main/kotlin"))
      add(CONFIGURATION_JACOCO_CLASSES, layout.buildDirectory.dir("tmp/kotlin-classes/debug").map { it.asFile })
      add(CONFIGURATION_JACOCO_EXEC, layout.buildDirectory.dir("outputs/unit_test_code_coverage").map { it.asFile })
      add(CONFIGURATION_JACOCO_EXEC, layout.buildDirectory.dir("outputs/code_coverage").map { it.asFile })
   }
}

fun Project.setupJacocoMergingPureKotlin() {
   registerJacocoConfigurations()

   if (project.name != "test") {
      artifacts {
         add(CONFIGURATION_JACOCO_SOURCES, layout.projectDirectory.dir("src/main/kotlin"))
         add(CONFIGURATION_JACOCO_CLASSES, layout.buildDirectory.dir("classes/kotlin/main").map { it.asFile })
         add(CONFIGURATION_JACOCO_EXEC, layout.buildDirectory.dir("jacoco").map { it.asFile })
      }
   }
}

private fun Project.registerJacocoConfigurations() {
   configurations.register(CONFIGURATION_JACOCO_SOURCES)
   configurations.register(CONFIGURATION_JACOCO_CLASSES)
   configurations.register(CONFIGURATION_JACOCO_EXEC)
}

@Suppress("UnstableApiUsage") // Isolated projects
fun Project.setupJacocoMergingRoot() {
   registerJacocoConfigurations()

   tasks.register("aggregatedJacocoReport", JacocoReport::class).apply {
      configure {
         classDirectories.from(
            configurations.getByName(CONFIGURATION_JACOCO_CLASSES).incoming.artifactView {
               isLenient = true
            }.files.flatMap { classDirectory ->
               fileTree(classDirectory) {
                  // Exclude release classes
                  exclude("**/release/**")

                  // Exclude generated classes
                  exclude("**/*ComposableSingletons*")
                  exclude("**/*MetroFactory*/**")
                  exclude("**/*MetroGraph*/**")
                  exclude("**/metro/hints/**")
                  exclude("**/android/showkase/**")
                  exclude("**/*PreviewKt.class")

                  // DI
                  exclude("**/*Providers.class")
                  exclude("**/*Providers$*.class")
               }
                  .files
            }
               // With KMM, there might be identical classes in some cases (for example, actual/expect). Filter them out.
               .distinctBy { it.name }
         )

         executionData.from(
            configurations.getByName(CONFIGURATION_JACOCO_EXEC).incoming.artifactView { isLenient = true }.files
               .map { execDirectory ->
                  fileTree(execDirectory) {
                     include("**/*.exec")
                     include("**/*.ec")
                  }
               },
            // Merge results from Firebase Test Lab instrumented tests
            project.layout.settingsDirectory.file(
               "instrumented_tests_results"
            )
               .let { fileTree(it) { include("*/*/artifacts/sdcard/Download/*.ec") } },
            // Merge results from Pull request
            project.layout.settingsDirectory.file(
               "coverage-data"
            )
               .let {
                  fileTree(it) {
                     include("**/*.ec")
                     include("**/*.exec")
                  }
               }
         )
         sourceDirectories.from(
            configurations.getByName(CONFIGURATION_JACOCO_SOURCES).incoming.artifactView { isLenient = true }.files
         )

         reports.xml.required.set(true)
      }
   }

   loadJacocoPathsFromSubprojects()
}

private fun Project.loadJacocoPathsFromSubprojects() {
   val rootProject = this

   subprojects {
      rootProject.dependencies.add(
         CONFIGURATION_JACOCO_CLASSES,
         rootProject.dependencies.project(
            mapOf(
               "path" to isolated.path,
               "configuration" to CONFIGURATION_JACOCO_CLASSES
            )
         )
      )

      rootProject.dependencies.add(
         CONFIGURATION_JACOCO_SOURCES,
         rootProject.dependencies.project(
            mapOf(
               "path" to isolated.path,
               "configuration" to CONFIGURATION_JACOCO_SOURCES
            )
         )
      )

      rootProject.dependencies.add(
         CONFIGURATION_JACOCO_EXEC,
         rootProject.dependencies.project(
            mapOf(
               "path" to isolated.path,
               "configuration" to CONFIGURATION_JACOCO_EXEC
            )
         )
      )
   }
}

const val CONFIGURATION_JACOCO_SOURCES = "jacocoSources"
const val CONFIGURATION_JACOCO_CLASSES = "jacocoClasses"
const val CONFIGURATION_JACOCO_EXEC = "jacocoExec"
