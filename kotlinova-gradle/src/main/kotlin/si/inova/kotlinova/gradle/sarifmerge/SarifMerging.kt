/*
 * Copyright 2025 INOVA IT d.o.o.
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

package si.inova.kotlinova.gradle.sarifmerge

import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Project
import si.inova.kotlinova.gradle.KotlinovaExtension
import java.io.File

internal fun Project.createTopLevelMergeTask() {
   rootProject.tasks.register("reportMerge", SarifMergeTask::class.java) { task ->
      task.outputs.cacheIf("IO bound task") { false }

      task.output.set(File(rootProject.rootDir, "merge.sarif"))

      task.doFirst {
         task.output.get().asFile.delete()
      }

      if (File(rootDir, "buildSrc").exists()) {
         task.input.from(File(rootDir, "buildSrc/build/reports/detekt/detekt.sarif"))
      }

      task.input.from(configurations.getByName(CONFIGURATION_SARIF_REPORT).incoming.artifactView {
         it.isLenient = true
      }.files)
   }

   @Suppress("UnstableApiUsage")
   subprojects {
      dependencies.add(
         CONFIGURATION_SARIF_REPORT,
         dependencies.project(
            mapOf(
               "path" to it.isolated.path,
               "configuration" to CONFIGURATION_SARIF_REPORT
            )
         )
      )
   }
}

internal fun Project.registerSarifMerging(extension: KotlinovaExtension) {
   project.configurations.create(CONFIGURATION_SARIF_REPORT)

   if (this@registerSarifMerging == rootProject) {
      createTopLevelMergeTask()
   }

   extension.tomlVersionBump.apply {
      afterEvaluate { _ ->
         if (extension.mergeDetektSarif.getOrElse(false)) {
            registerDetektSarifMerging()
         }
         if (extension.mergeAndroidLintSarif.getOrElse(false)) {
            registerAndroidLintSarifMerging()
         }
      }
   }
}

private fun Project.registerDetektSarifMerging() {
   // Artifact/configuration only works when the task succeeds. Using it normally would mean that merging would fail if
   // any of the detekt tasks fail, which completely defeats the purpose. That's why detekt artifact actually depends on another
   // task that always succeeds which is marked as a finalizer for the detekt task.

   val finalDetekt = tasks.register("finalDetekt")

   tasks.withType(Detekt::class.java).configureEach { detektTask ->
      // We need to set basePath to ensure sarif files have relative path in them
      detektTask.basePath = this.rootDir.absolutePath

      detektTask.reports {
         it.sarif.required.set(true)
      }

      artifacts {
         it.add(CONFIGURATION_SARIF_REPORT, detektTask.sarifReportFile) { artifact ->
            artifact.builtBy(finalDetekt)
         }
      }

      detektTask.finalizedBy(finalDetekt)
   }
}

internal const val CONFIGURATION_SARIF_REPORT = "sarifReport"
