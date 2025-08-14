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

plugins {
   id("com.android.library")
   id("org.jetbrains.kotlin.android")
   id("com.google.devtools.ksp")
   id("kotlin-parcelize")
   id("org.jetbrains.kotlin.plugin.compose")
   alias(libs.plugins.metro)
}

android {
   namespace = "si.inova.kotlinova.navigation.conditional"
   compileSdk = 35

   defaultConfig {
      minSdk = 24
      targetSdk = 34
   }

   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_21
      targetCompatibility = JavaVersion.VERSION_21
   }
}

metro {
   enableScopedInjectClassHints = true
}

dependencies {
   implementation(projects.keys)

   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.compose.ui)
   implementation(libs.androidx.compose.ui.graphics)
   implementation(libs.androidx.compose.ui.tooling.preview)
   implementation(libs.androidx.compose.material3)
   implementation(libs.kotlinova.navigation)

   ksp(libs.kotlinova.navigation.compiler)

   debugImplementation(libs.androidx.compose.ui.test.manifest)
   debugImplementation(libs.androidx.compose.ui.tooling)
}
