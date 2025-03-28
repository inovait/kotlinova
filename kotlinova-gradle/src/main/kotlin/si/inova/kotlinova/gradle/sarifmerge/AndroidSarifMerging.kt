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

import com.android.build.gradle.internal.lint.AndroidLintTask
import org.gradle.api.Project

internal fun Project.registerAndroidLintSarifMerging() {
   // Artifact/configuration only works when the task succeeds. Using it normally would mean that merging would fail if
   // any of the lint tasks fail, which completely defeats the purpose. That's why lint artifact actually depends on another
   // task that always succeeds which is marked as a finalizer for the lint task.
   val finalLint = tasks.register("finalLint")

   tasks.withType(AndroidLintTask::class.java).configureEach { lintTask ->
      val variant = lintTask.variantName
      val lintSarifFile = lintTask.project.layout.buildDirectory.file(
         "reports/lint-results-$variant.sarif"
      )

      artifacts {
         it.add(CONFIGURATION_SARIF_REPORT, lintSarifFile) { artifact ->
            artifact.builtBy(finalLint)
         }
      }

      lintTask.finalizedBy(finalLint)
   }
}
