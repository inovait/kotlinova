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

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.accessors.dm.LibrariesForLibs
import si.inova.kotlinova.gradle.KotlinovaExtension
import util.commonAndroid
import util.isAndroidProject

val libs = the<LibrariesForLibs>()

plugins {
   id("kotlinova")
}

apply(plugin = "dev.detekt")

if (isAndroidProject()) {
   commonAndroid {
      lint {
         lintConfig = file("$rootDir/config/android-lint.xml")
         abortOnError = true

         warningsAsErrors = true
      }
   }
}

configure<KotlinovaExtension> {
   mergeDetektSarif = true
   enableDetektPreCommitHook = true

   if (isAndroidProject()) {
      mergeAndroidLintSarif = true
   }
}

configure<DetektExtension> {
   config.from(files("$rootDir/config/detekt.yml"))
}

tasks.withType<Detekt>().configureEach {
   val buildDir = project.layout.buildDirectory.asFile.get().absolutePath
   // Exclude all generated files
   exclude {
      it.file.absolutePath.contains(buildDir)
   }
}

dependencies {
   add("detektPlugins", libs.detekt.ktlint)
}
