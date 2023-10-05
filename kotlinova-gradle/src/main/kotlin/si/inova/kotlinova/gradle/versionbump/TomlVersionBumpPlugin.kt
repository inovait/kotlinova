/*
 * Copyright 2023 INOVA IT d.o.o.
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

package si.inova.kotlinova.gradle.versionbump

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject
import org.tomlj.Toml
import org.tomlj.TomlTable
import java.io.File

class TomlVersionBumpPlugin : Plugin<Project> {
   override fun apply(project: Project) {
      val extension = project.extensions.create("tomlVersionBump", TomlVersionBumpExtension::class.java)

      project.tasks.register("updateLibsToml", UpdateTomlLibs::class.java) {
         it.reportFiles = extension.versionReportFiles.get()

         it.tomlFile = extension.tomlFile.get()
      }
   }
}

open class UpdateTomlLibs : DefaultTask() {
   @InputFiles
   lateinit var reportFiles: FileTree

   @InputFile
   lateinit var tomlFile: File

   @TaskAction
   fun execute() {
      val requestedUpdates = parseDependencyUpdates()

      val originalTomlText = tomlFile.readText()

      val libsToml = Toml.parse(originalTomlText)
      if (libsToml.hasErrors()) {
         for (error in libsToml.errors()) {
            error.printStackTrace()
         }

         error("TOML parsing of the $tomlFile failed")
      }

      val libraries = requireNotNull(libsToml.getTable("libraries")) {
         "$tomlFile does not contain libraries section"
      }

      val targetBumps = HashMap<String, String>()

      for (entry in libraries.entrySet()) {
         val tomlValue = entry.value as TomlTable
         val module = tomlValue.get("module")
         val versionRef = tomlValue.get("version.ref") as? String?
            ?: continue
         val targetVersion = requestedUpdates[module]

         if (targetVersion != null) {
            targetBumps[versionRef] = targetVersion
         }
      }

      if (targetBumps.isEmpty()) {
         println("Nothing to update")
         return
      }

      var updatedText = originalTomlText
      for (bump in targetBumps) {
         val currentVersion = libsToml.get("versions.${bump.key}") as? String?
            ?: error("Missing ${bump.key} in the toml file. Is the toml file valid?")

         println("Bumping ${bump.key} from $currentVersion to ${bump.value}")

         updatedText = updatedText.replace("${bump.key} = \"$currentVersion\"", "${bump.key} = \"${bump.value}\"")
      }

      tomlFile.writeText(updatedText)
   }

   private fun parseDependencyUpdates(): Map<String, String> {
      val requestedUpdates = HashMap<String, String>()
      val failedUpdates = HashSet<String>()

      for (dependencyReport in reportFiles) {
         val jsonString = dependencyReport.readText()
         val obj = JSONObject(jsonString)

         obj.getJSONObject("outdated").getJSONArray("dependencies").forEach {
            val dependencyObj = it as JSONObject

            val group = dependencyObj.getString("group")
            val name = dependencyObj.getString("name")

            val available = dependencyObj.getJSONObject("available")

            val newVersion = available.getStringOrNull("release")
               ?: available.getStringOrNull("milestone")
               ?: available.getStringOrNull("integration")
               ?: return@forEach

            requestedUpdates["$group:$name"] = newVersion
         }

         obj.getJSONObject("unresolved").getJSONArray("dependencies").forEach {
            val dependencyObj = it as JSONObject

            val group = dependencyObj.getString("group")
            val name = dependencyObj.getString("name")

            failedUpdates += "$group:$name"
         }
      }

      for (failedUpdate in failedUpdates) {
         println(
            "WARNING: Failed to check for updates for '$failedUpdate'. " +
               "Please check and update manually. See dependencyUpdates output for more info."
         )
      }

      return requestedUpdates
   }

   private fun JSONObject.getStringOrNull(key: String): String? {
      return if (isNull(key)) {
         null
      } else {
         getString(key)
      }
   }
}
