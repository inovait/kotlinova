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

import dev.detekt.gradle.Detekt
import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.VerificationType
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.TaskProvider
import si.inova.kotlinova.gradle.KotlinovaExtension
import java.io.File

internal fun Project.createTopLevelMergeTask() {
   val aggregationConfiguration = project.configurations.dependencyScope("sarifReportAggregation")
   val resultsConfiguration = project.configurations.resolvable("sarifReportResults") { cofiguration ->
      cofiguration.extendsFrom(aggregationConfiguration.get())

      cofiguration.attributes {
         it.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.VERIFICATION))
         it.attribute(
            VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
            objects.named(VerificationType::class.java, VERIFICATION_TYPE_SARIF)
         )
      }
   }

   rootProject.tasks.register("reportMerge", SarifMergeTask::class.java) { task ->
      task.output.set(File(rootProject.rootDir, "merge.sarif"))

      task.doFirst {
         task.output.get().asFile.delete()
      }

      if (File(rootDir, "buildSrc").exists()) {
         task.input.from(File(rootDir, "buildSrc/build/reports/detekt/detekt.sarif"))
      }

      task.input.from(
         resultsConfiguration.map { config ->
            config.incoming
               .artifactView {
                  it.isLenient = true
               }
               .files
         }
      )
   }

   subprojects.forEach {
      dependencies.add(
         "sarifReportAggregation",
         dependencies.project(
            mapOf(
               "path" to it.isolated.path,
            )
         )
      )
   }
}

internal fun Project.registerSarifMerging(extension: KotlinovaExtension) {
   if (this@registerSarifMerging == rootProject) {
      createTopLevelMergeTask()
   } else {
      // Configuration/artifacts + configureEach do not mix.
      // Tasks that have Android variants (such as detektDebug, lintRelease etc.) sometimes
      // get configured after artifacts are already read and thus frozen in place.
      // That's why we cannot expose sarif files directly from the detekt/lint tasks, we must create a middleman task
      // that does not need variant resolving and that tasks then merges all local sarif files into a single one
      // that can get consumed by the root project

      val sarifFiles = project.objects.fileCollection()

      val localSarifMergeTask = tasks.register("reportMerge", SarifMergeTask::class.java) { task ->
         task.output.set(project.layout.buildDirectory.file("merge.sarif"))
         task.input.from(sarifFiles)
      }

      val dataElementsVariant = project.configurations.consumable("sarifReportElements") { configuration ->
         configuration.attributes {
            it.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.VERIFICATION))
            it.attribute(
               VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
               objects.named(VerificationType::class.java, VERIFICATION_TYPE_SARIF)
            )
         }
      }

      dataElementsVariant.configure { configuration ->
         configuration.outgoing.artifact(localSarifMergeTask.map { it.output })
      }

      extension.tomlVersionBump.apply {
         afterEvaluate { _ ->
            if (extension.mergeDetektSarif.getOrElse(false)) {
               registerDetektSarifMerging(localSarifMergeTask, sarifFiles)
            }
            if (extension.mergeAndroidLintSarif.getOrElse(false)) {
               registerAndroidLintSarifMerging(localSarifMergeTask, sarifFiles)
            }
         }
      }
   }
}

private fun Project.registerDetektSarifMerging(
   localSarifMergeTask: TaskProvider<SarifMergeTask>,
   sarifFiles: ConfigurableFileCollection
) {
   tasks.withType(Detekt::class.java).configureEach { detektTask ->
      // We need to set basePath to ensure sarif files have relative path in them
      detektTask.basePath.set(this.rootDir.absolutePath)

      detektTask.reports {
         it.sarif.required.set(true)
      }

      sarifFiles.from(
         detektTask.reports.sarif.outputLocation.orElse { error("task ${detektTask.path} did not expose a sarif file") }
      )

      detektTask.finalizedBy(localSarifMergeTask)
   }
}

private const val VERIFICATION_TYPE_SARIF = "lint_results_sarif"
