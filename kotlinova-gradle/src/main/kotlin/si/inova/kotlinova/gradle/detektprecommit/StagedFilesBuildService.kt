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

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File
import javax.inject.Inject

internal abstract class StagedFilesBuildService @Inject constructor(providerFactory: ProviderFactory) :
   BuildService<BuildServiceParameters.None> {
   val stagedGitFiles: Provider<List<File>> = providerFactory.of(StagedFilesValueSource::class.java) {}
}

internal abstract class StagedFilesValueSource : ValueSource<List<File>, ValueSourceParameters.None> {
   override fun obtain(): List<File>? {
      val gitRootProcess =
         ProcessBuilder("git", "--no-pager", "rev-parse", "--git-dir").start()
      val gitRootError = gitRootProcess.errorStream.readBytes().decodeToString()
      if (gitRootError.isNotBlank()) {
         error("Git error : $gitRootError")
      }

      val gitDirectory = gitRootProcess.inputStream.readBytes().decodeToString().trim()
      val gitRootDirectory = if (gitDirectory == ".git") {
         // If git outputs just ".git", then our current directory is the root directory of the git repo
         System.getProperty("user.dir")
      } else {
         gitDirectory.removeSuffix("/.git")
      }

      val gitFileListProcess =
         ProcessBuilder("git", "--no-pager", "diff", "--name-only", "--cached").start()
      val gitFileListError = gitFileListProcess.errorStream.readBytes().decodeToString()
      if (gitFileListError.isNotBlank()) {
         error("Git error : $gitFileListError")
      }

      val gitFiles = gitFileListProcess.inputStream.readBytes().decodeToString().trim().split("\n")

      return gitFiles.map {
         File("$gitRootDirectory/$it")
      }
   }
}
