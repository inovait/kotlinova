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

import io.github.detekt.sarif4k.Level
import io.github.detekt.sarif4k.ReportingConfiguration
import io.github.detekt.sarif4k.SarifSerializer
import io.github.detekt.sarif4k.merge
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * A gradle task that merges sarif reports into one.
 *
 * Based on https://github.com/detekt/detekt/blob/ace6e4e5de07532416e89abf811ba37e0ea2b565/detekt-gradle-plugin/src/main/kotlin/io/gitlab/arturbosch/detekt/report/ReportMergeTask.kt
 */
@DisableCachingByDefault(because = "IO Bound task")
abstract class SarifMergeTask : DefaultTask() {
   @get:InputFiles
   @get:PathSensitive(PathSensitivity.RELATIVE)
   abstract val input: ConfigurableFileCollection

   @get:OutputFile
   abstract val output: RegularFileProperty

   @TaskAction
   fun merge() {
      val inputFiles = input.files.filter { it.exists() }

      if (inputFiles.isEmpty()) {
         output.get().asFile.delete()
      } else if (inputFiles.size == 1) {
         inputFiles.first().copyTo(output.get().asFile, overwrite = true)
      } else {
         val sarifFiles = inputFiles.map { SarifSerializer.fromJson(it.readText()) }

         val merged = sarifFiles.reduce { a, b -> a.merge(b) }

         val fixedSeverity = merged.copy(
            runs = merged.runs.map { run ->
               // Workaround for the https://github.com/security-alert/security-alert/issues/50
               run.copy(
                  tool = run.tool.copy(
                     driver = run.tool.driver.copy(
                        rules = run.tool.driver.rules?.map { rule ->
                           if (rule.defaultConfiguration?.level == null) {
                              rule.copy(
                                 defaultConfiguration = ReportingConfiguration(level = Level.Warning)
                              )
                           } else {
                              rule
                           }
                        }
                     )
                  )
               )
            }.sortedByDescending {
               // Display runs with fewer errors last
               // Workaround for https://github.com/security-alert/security-alert/issues/74
               it.results?.size ?: 0
            }
         )

         output.get().asFile.writeText(SarifSerializer.toJson(fixedSeverity))
      }
   }
}
