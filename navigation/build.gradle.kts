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

import util.publishLibrary

plugins {
   androidLibraryModule
   id("kotlin-parcelize")
   id("com.google.devtools.ksp")
   id("org.jetbrains.kotlin.plugin.compose")
}

publishLibrary(
   userFriendlyName = "Navigation",
   description = "Anvil-based navigation system",
   githubPath = "navigation"
)

android {
   namespace = "si.inova.kotlinova.navigation"
}

dependencies {
   api(libs.simpleStack)
   api(libs.kotlinInject.runtime) // This needs to be API to ensure generated files compile
   api(libs.kotlinInject.anvil.runtime) // This needs to be API to ensure generated files compile
   api(libs.androidx.activity.compose)

   implementation(projects.kotlinova.core)

   implementation(libs.androidx.core)
   implementation(libs.androidx.compose.animation)
   implementation(libs.androidx.compose.ui)
   implementation(libs.androidx.compose.ui.graphics)
   implementation(libs.androidx.compose.ui.tooling.preview)
   implementation(libs.androidx.compose.ui.util)
   implementation(libs.androidx.compose.material3)
   implementation(libs.androidx.lifecycle.compose)
   implementation(libs.androidx.lifecycle.viewModel.compose)
   implementation(libs.kotlinInject.anvil.annotations)

   ksp(libs.kotlinInject.compiler)
   ksp(libs.kotlinInject.anvil.compiler)
   ksp(projects.navigation.navigationCompiler)

   debugImplementation(libs.androidx.compose.ui.tooling)
   debugImplementation(libs.androidx.compose.ui.test.manifest)

   testImplementation(projects.navigation.navigationTest)
   testImplementation(libs.turbine)
   testImplementation(libs.kotlin.coroutines.test)
   testImplementation(libs.kotest.assertions)
   testImplementation(libs.junit5.api)
   testRuntimeOnly(libs.junit5.engine)
   testRuntimeOnly(libs.junit5.launcher)
}
