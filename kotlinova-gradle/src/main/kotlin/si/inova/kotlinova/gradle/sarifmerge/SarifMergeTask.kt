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

import io.github.detekt.sarif4k.Level
import io.github.detekt.sarif4k.PropertyBag
import io.github.detekt.sarif4k.ReportingConfiguration
import io.github.detekt.sarif4k.Run
import io.github.detekt.sarif4k.SarifSchema210
import io.github.detekt.sarif4k.SarifSerializer
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * A gradle task that merges sarif reports into one.
 *
 * Based on https://github.com/detekt/detekt/blob/ace6e4e5de07532416e89abf811ba37e0ea2b565/detekt-gradle-plugin/src/main/kotlin/io/gitlab/arturbosch/detekt/report/ReportMergeTask.kt
 */
@CacheableTask
abstract class SarifMergeTask : DefaultTask() {
   @get:InputFiles
   @get:PathSensitive(PathSensitivity.RELATIVE)
   abstract val input: ConfigurableFileCollection

   @get:OutputFile
   abstract val output: RegularFileProperty

   @TaskAction
   fun merge() {
      val sarifFiles =
         input.files.filter { it.exists() }.map { SarifSerializer.fromJson(it.readText()) }
      if (sarifFiles.isEmpty()) {
         output.get().asFile.delete()

         return
      }

      val merged = sarifFiles.reduce(SarifSchema210::merge)

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

private fun SarifSchema210.merge(other: SarifSchema210): SarifSchema210 {
   require(schema == other.schema) {
      "Cannot merge sarifs with different schemas: '${schema ?: "null"}' or '${other.schema ?: "null"}'"
   }
   require(version == other.version) {
      "Cannot merge sarifs with different versions: '$version' or '${other.version}'"
   }

   val mergedExternalProperties = (inlineExternalProperties.orEmpty() + other.inlineExternalProperties.orEmpty())
      .takeIf { it.isNotEmpty() }

   val mergedProperties = properties?.merge(other.properties)

   val mergedRuns = runs.merge(other.runs)

   return SarifSchema210(
      schema,
      version,
      mergedExternalProperties,
      mergedProperties,
      mergedRuns
   )
}

// Temporary copy from https://github.com/detekt/sarif4k/pull/87 until new version of sarif4k with that included is released

private fun PropertyBag.merge(other: PropertyBag?): PropertyBag {
   val aTags = this["tags"] as? Collection<*>
   val bTags = (other?.get("tags") as? Collection<*>).orEmpty()
   val tags = if (aTags != null) {
      mapOf("tags" to (aTags + bTags).distinct())
   } else {
      emptyMap()
   }

   return PropertyBag(this + other.orEmpty() + tags)
}

private fun List<Run>.merge(other: List<Run>): List<Run> {
   val runsByTool = (this + other).groupBy { it.tool.driver.fullName }

   return runsByTool.mapValues { (_, runs) ->
      val baseRun = runs.firstOrNull() ?: return@mapValues null

      val mergedResults = runs.flatMap { it.results.orEmpty() }
      val mergedRules = runs.flatMap { it.tool.driver.rules.orEmpty() }.distinctBy { it.id }

      baseRun.copy(
         results = mergedResults,
         tool = baseRun.tool.copy(
            driver = baseRun.tool.driver.copy(
               rules = mergedRules
            )
         )
      )
   }.values.filterNotNull().toList()
}
