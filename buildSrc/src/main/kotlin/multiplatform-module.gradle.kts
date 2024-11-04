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

import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.dokka.gradle.DokkaTask

val libs = the<LibrariesForLibs>()

plugins {
   id("com.android.library")
   id("android-commons")
   kotlin("multiplatform")
   id("kotlin-parcelize")
}

kotlin {
   androidTarget {
      publishLibraryVariants("release")
   }

   jvmToolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
   }

   jvm {

      testRuns["test"].executionTask.configure {
         useJUnitPlatform()
      }
   }

   @Suppress("UNUSED_VARIABLE")
   sourceSets {
      all {
         languageSettings {
            optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            optIn("kotlinx.coroutines.FlowPreview")
         }
      }
      val commonMain by getting
      val jvmCommon by creating {
         dependsOn(commonMain)
      }
      val jvmMain by getting {
         dependsOn(jvmCommon)
      }
      val jvmTest by getting {
         dependencies {
            implementation(libs.turbine)
            implementation(libs.kotlin.coroutines.test)
            implementation(libs.kotest.assertions)
            implementation(libs.junit5.api)
            runtimeOnly(libs.junit5.engine)
         }
      }
      val androidMain by getting {
         dependsOn(jvmCommon)
      }
   }
}

val javadocJar: TaskProvider<Jar> = tasks.register("javadocJar", Jar::class.java) {
   val dokkaJavadocTask = tasks.getByName("dokkaJavadoc", DokkaTask::class)
   dependsOn(dokkaJavadocTask)
   archiveClassifier.set("javadoc")
   from(dokkaJavadocTask.outputDirectory)
}

publishing {
   publications.withType<MavenPublication> {
      artifact(javadocJar)
   }
}
