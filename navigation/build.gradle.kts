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

import util.publishLibrary

plugins {
   multiplatformModule
   id("kotlinx-serialization")
   id("org.jetbrains.kotlin.plugin.compose")
   alias(libs.plugins.metro)
   unmock
}

publishLibrary(
   userFriendlyName = "Navigation",
   description = "Metro-based navigation system",
   githubPath = "navigation"
)

android {
   namespace = "si.inova.kotlinova.navigation"
}

kotlin {
   sourceSets {
      androidMain {
         dependencies {
            api(libs.androidx.activity.compose)
            implementation(libs.androidx.core)
         }
      }
      commonMain {
         dependencies {
            api(libs.androidx.savedState)

            implementation(libs.composeMultiplatform.animation)
            implementation(libs.composeMultiplatform.ui)
            implementation(libs.composeMultiplatform.material3)
            implementation(libs.androidx.lifecycle.viewModel.compose)
            implementation(libs.kotlin.serialization)
         }
      }
      val nonAndroidMain by creating {
         dependsOn(commonMain.get())
      }

      jvmMain {
         dependsOn(nonAndroidMain)
      }

      nativeMain {
         dependsOn(nonAndroidMain)
      }

      androidUnitTest {
         dependencies {
            implementation(projects.navigation.navigationTest)
            implementation(libs.turbine)
            implementation(libs.kotlin.coroutines.test)
            implementation(libs.kotest.assertions)
            implementation(libs.junit5.api)
            runtimeOnly(libs.junit5.engine)
            runtimeOnly(libs.junit5.launcher)
         }
      }
   }
}
