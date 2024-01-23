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

package si.inova.kotlinova.gradle.detektprecommit

import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import si.inova.kotlinova.gradle.KotlinovaExtension
import java.io.File
import java.io.FileNotFoundException

internal fun Project.registerDetektPreCommitHook(extension: KotlinovaExtension) {
   afterEvaluate {
      if (extension.enableDetektPreCommitHook.getOrElse(false)) {
         val gitPreCommitFileListTask =
            tasks.register("gitPreCommitFileList", GitPreCommitFilesTask::class.java) { task ->
               val targetFile = File(
                  project.layout.buildDirectory.asFile.get(),
                  "intermediates/gitPreCommitFileList/output"
               )

               targetFile.also {
                  it.parentFile.mkdirs()
                  task.gitStagedListFile.set(it)
               }
               task.outputs.upToDateWhen { false }
            }

         tasks.withType(Detekt::class.java).configureEach { detektTask ->
            if (project.hasProperty("precommit")) {
               detektTask.dependsOn(gitPreCommitFileListTask)

               val rootDir = project.rootDir
               val projectDir = projectDir

               val fileCollection = files()

               detektTask.setSource(
                  gitPreCommitFileListTask.flatMap { task ->
                     task.getStagedFiles(rootDir)
                        .map { stagedFiles ->
                           val stagedFilesFromThisProject = stagedFiles
                              .filter { it.startsWith(projectDir) }

                           fileCollection.setFrom(*stagedFilesFromThisProject.toTypedArray())

                           fileCollection.asFileTree
                        }
                  }
               )
            }
         }
      }
   }
}

abstract class GitPreCommitFilesTask : DefaultTask() {
   @get:OutputFile
   abstract val gitStagedListFile: RegularFileProperty

   @TaskAction
   fun taskAction() {
      val gitProcess =
         ProcessBuilder("git", "--no-pager", "diff", "--name-only", "--cached").start()
      val error = gitProcess.errorStream.readBytes().decodeToString()
      if (error.isNotBlank()) {
         error("Git error : $error")
      }

      val gitVersion = gitProcess.inputStream.readBytes().decodeToString().trim()

      gitStagedListFile.get().asFile.writeText(gitVersion)
   }
}

fun GitPreCommitFilesTask.getStagedFiles(rootDir: File): Provider<List<File>> {
   return gitStagedListFile.map { gitFile ->
      try {
         gitFile.asFile.readLines()
            .filter { it.isNotBlank() }
            .map { File(rootDir, it) }
      } catch (e: FileNotFoundException) {
         // See https://github.com/gradle/gradle/issues/19252
         throw IllegalStateException(
            "Failed to load git configuration. " +
               "Please disable configuration cache for the first commit and " +
               "try again",
            e
         )
      }
   }
}
