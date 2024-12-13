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
import org.gradle.api.Project
import si.inova.kotlinova.gradle.KotlinovaExtension

internal fun Project.registerDetektPreCommitHook(extension: KotlinovaExtension) {
   afterEvaluate {
      if (extension.enableDetektPreCommitHook.getOrElse(false)) {
         val serviceProvider = gradle.sharedServices.registerIfAbsent("gitStagedFiles", StagedFilesBuildService::class.java)

         tasks.withType(Detekt::class.java).configureEach { detektTask ->
            detektTask.usesService(serviceProvider)

            if (project.hasProperty("precommit")) {
               val fileCollection = files()

               val originalSource = detektTask.source

               detektTask.setSource(
                  serviceProvider.flatMap { it.stagedGitFiles }.map { stagedFiles ->
                     val stagedFilesFromThisProject = stagedFiles
                        .filter { originalSource.contains(it) }

                     fileCollection.setFrom(*stagedFilesFromThisProject.toTypedArray())

                     fileCollection.asFileTree
                  }
               )
            }
         }
      }
   }
}
