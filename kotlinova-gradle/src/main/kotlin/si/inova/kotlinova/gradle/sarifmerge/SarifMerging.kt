/*
 * Copyright 2024 INOVA IT d.o.o.
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

import com.android.build.gradle.internal.lint.AndroidLintTask
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.TaskProvider
import si.inova.kotlinova.gradle.KotlinovaExtension
import java.io.File

internal fun Project.createTopLevelMergeTask(): TaskProvider<ReportMergeTask> {
   // This violates build isolation but we are forced to use it for performance reasons
   // Waiting for https://github.com/gradle/gradle/issues/25179 for a possible workaround

   return try {
      rootProject.tasks.named("reportMerge", ReportMergeTask::class.java)
   } catch (ignored: UnknownTaskException) {
      rootProject.tasks.register("reportMerge", ReportMergeTask::class.java) { task ->
         task.output.set(File(rootProject.rootDir, "merge.sarif"))

         task.doFirst {
            task.output.get().asFile.delete()
         }

         if (File(rootDir, "buildSrc").exists()) {
            task.input.from(File(rootDir, "buildSrc/build/reports/detekt/detekt.sarif"))
         }
      }
   }
}

internal fun Project.registerSarifMerging(extension: KotlinovaExtension) {
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
   val mergeTaskProvider = createTopLevelMergeTask()

   val detektOutputs = objects.listProperty(RegularFile::class.java)

   tasks.withType(Detekt::class.java).configureEach { detektTask ->
      // We need to set basePath to ensure sarif files have relative path in them
      detektTask.basePath = this.rootDir.absolutePath

      detektTask.reports {
         it.sarif.required.set(true)
      }

      detektOutputs.add(detektTask.sarifReportFile)

      detektTask.finalizedBy(mergeTaskProvider)
   }

   mergeTaskProvider.configure { mergeTask ->
      mergeTask.input.from(detektOutputs)
   }
}

private fun Project.registerAndroidLintSarifMerging() {
   val mergeTaskProvider = createTopLevelMergeTask()

   val lintOutputs = objects.listProperty(RegularFile::class.java)

   tasks.withType(AndroidLintTask::class.java).configureEach { lintTask ->
      val variant = lintTask.variantName
      val lintSarifFile = lintTask.project.layout.buildDirectory.file(
         "reports/lint-results-$variant.sarif"
      )

      lintOutputs.add(lintSarifFile)

      lintTask.finalizedBy(mergeTaskProvider)
   }

   mergeTaskProvider.configure { mergeTask ->
      mergeTask.input.from(lintOutputs)
   }
}
