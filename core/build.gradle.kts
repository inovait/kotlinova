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

import util.publishLibrary

plugins {
   multiplatformModule
}

android {
   namespace = "si.inova.kotlinova.core"
}

publishLibrary(
   userFriendlyName = "kotlinova-core",
   description = "A collection of core utilities",
   githubPath = "core"
)

kotlin {
   sourceSets {
      val androidMain by getting {
         dependencies {
            implementation(libs.androidx.core)

            compileOnly(libs.androidx.compose.runtime)
         }
      }
      val commonMain by getting {
         dependencies {
            implementation(libs.kotlin.coroutines)
            implementation(libs.dispatch)
         }
      }
      val jvmTest by getting {
         dependencies {
            implementation(projects.kotlinova.core.test)
         }
      }
   }
}
