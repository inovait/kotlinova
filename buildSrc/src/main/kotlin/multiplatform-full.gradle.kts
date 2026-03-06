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

@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

val libs = the<LibrariesForLibs>()

plugins {
   id("multiplatform-jvm")
}

kotlin {
   iosArm64()
   iosSimulatorArm64()
   macosArm64()
   wasmJs() {
      browser()
      nodejs()
      d8()
   }

   // Native desktop not supported until
   // https://youtrack.jetbrains.com/projects/CMP/issues/CMP-1923/Kotlin-Native-Support-for-Desktop
   // is ready

   sourceSets {
      val commonMain by getting

      val nonJvmMain by creating {
         dependsOn(commonMain)
      }
      val nonAndroidMain by creating {
         dependsOn(commonMain)
      }

      nativeMain {
         dependsOn(nonJvmMain)
         dependsOn(nonAndroidMain)
      }

      wasmJsMain {
         dependsOn(nonJvmMain)
         dependsOn(nonAndroidMain)
      }

      jvmMain {
         dependsOn(nonAndroidMain)
      }
   }
}
