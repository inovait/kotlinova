import util.publishLibrary

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

plugins {
   multiplatformModule
}

android {
   namespace = "si.inova.kotlinova.retrofit"
}

publishLibrary(
   userFriendlyName = "kotlinova-retrofit",
   description = "A collection of utilities for retrofit requests",
   githubPath = "retrofit"
)

kotlin {
   sourceSets {
      androidMain {
         dependencies {
            implementation(libs.dagger.runtime)
            implementation(libs.androidx.core)

            compileOnly(libs.androidx.compose.runtime)
         }
      }
      jvmMain {
         dependencies {
            api(libs.okhttp)
            api(libs.moshi)
            api(libs.retrofit)

            implementation(projects.core)
            implementation(libs.dispatch)
            implementation(libs.retrofit.moshi)
            implementation(libs.kotlin.coroutines)
         }
      }
      jvmTest {
         dependencies {
            implementation(projects.core.test)
            implementation(projects.retrofit.retrofitTest)
            implementation(libs.turbine)
         }
      }
   }
}
