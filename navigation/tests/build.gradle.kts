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

// We cannot use test module directly (https://issuetracker.google.com/issues/201899739),
// so we just use an empty app module with android tests

plugins {
   id("com.android.application")
   kotlin("android")
   androidCommon
   id("com.google.devtools.ksp")
   id("kotlin-parcelize")
   id("org.jetbrains.kotlin.plugin.compose")
   alias(libs.plugins.metro)
}

android {
   namespace = "si.inova.kotlinova.navigation.tests"

   defaultConfig {
      applicationId = namespace
      versionCode = 1
      versionName = "1.0"
      targetSdk = 33
   }
}

dependencies {
   androidTestImplementation(projects.kotlinova.navigation)
   androidTestImplementation(projects.kotlinova.navigation.navigationFragment)

   implementation(libs.androidx.core)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.fragment)
   implementation(libs.androidx.compose.ui)
   implementation(libs.androidx.compose.ui.graphics)
   implementation(libs.androidx.compose.ui.test.manifest)
   implementation(libs.androidx.compose.ui.tooling)
   implementation(libs.androidx.compose.ui.tooling.preview)
   implementation(libs.androidx.compose.ui.util)
   implementation(libs.androidx.compose.material3)
   implementation(libs.androidx.lifecycle.compose)
   implementation(libs.androidx.lifecycle.viewModel.compose)

   kspAndroidTest(projects.navigation.navigationCompiler)

   androidTestImplementation(libs.junit4)
   androidTestImplementation(libs.androidx.compose.ui.test.junit4)
   androidTestImplementation(libs.androidx.test.espresso)
   androidTestImplementation(libs.kotest.assertions)
}
