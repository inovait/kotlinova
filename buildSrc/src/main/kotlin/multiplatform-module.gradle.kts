/*
 * Copyright 2026 INOVA IT d.o.o.
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

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("com.android.library")
   kotlin("multiplatform")
   id("android-commons")
   id("kotlin-parcelize")
}

kotlin {
   androidTarget()

   jvm {
      testRuns["test"].executionTask.configure {
         useJUnitPlatform()
      }
   }

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
            runtimeOnly(libs.junit5.launcher)
         }
      }
      val androidMain by getting {
         dependsOn(jvmCommon)
      }
   }
}

mavenPublishing {
   configure(
      KotlinMultiplatform(
         javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationJavadoc"),
         sourcesJar = true,
         androidVariantsToPublish = listOf("release")
      )
   )
}
