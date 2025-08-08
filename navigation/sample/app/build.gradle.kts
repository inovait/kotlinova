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
   id("com.android.application")
   id("org.jetbrains.kotlin.android")
   id("com.google.devtools.ksp")
   id("org.jetbrains.kotlin.plugin.compose")
}

android {
   namespace = "si.inova.kotlinova.navigation.sample"

   compileSdk = 35

   defaultConfig {
      applicationId = "si.inova.kotlinova.navigation.sample"
      minSdk = 24
      targetSdk = 34
      versionCode = 1
      versionName = "1.0"
   }

   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_21
      targetCompatibility = JavaVersion.VERSION_21
   }

   kotlinOptions {

   }

   packaging {
      resources {
         excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
   }
}

kotlin {
   jvmToolchain(21)
}

dependencies {
   implementation(projects.fragment)
   implementation(projects.conditional)
   implementation(projects.keys)
   implementation(projects.mainScreen)
   implementation(projects.nested)
   implementation(projects.sharedViewmodel)
   implementation(projects.slideAnimation)
   implementation(projects.sharedTransition)
   implementation(projects.tabs)

   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.compose.ui)
   implementation(libs.androidx.compose.ui.graphics)
   implementation(libs.androidx.compose.ui.tooling.preview)
   implementation(libs.androidx.compose.material3)
   implementation(libs.androidx.lifecycle.runtime)
   implementation(libs.androidx.fragment)
   implementation(libs.androidx.core)
   implementation(libs.kotlinova.navigation)
   implementation(libs.kotlinInject.runtime)
   implementation(libs.kotlinInject.anvil.annotations)
   implementation(libs.kotlinInject.anvil.runtime)

   ksp(libs.kotlinInject.compiler)
   ksp(libs.kotlinInject.anvil.compiler)
   ksp(libs.kotlinova.navigation.compiler)

   debugImplementation(libs.androidx.compose.ui.test.manifest)
   debugImplementation(libs.androidx.compose.ui.tooling)
}
