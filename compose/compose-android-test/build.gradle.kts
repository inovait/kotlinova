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
   androidLibraryModule
   id("org.jetbrains.kotlin.plugin.compose")
}

publishLibrary(
   userFriendlyName = "Compose Android Test",
   description = "Set of utilities for instrumented tests with Jetpack Compose",
   githubPath = "compose"
)

android {
   namespace = "si.inova.kotlinova.compose.androidtest"
}

dependencies {
   api(projects.kotlinova.compose)
   api(projects.kotlinova.retrofit.retrofitTest)
   api(libs.dispatch.espresso)

   implementation(projects.kotlinova.core)
   implementation(projects.kotlinova.core.test)
   implementation(libs.androidx.compose.ui)
   implementation(libs.androidx.compose.ui.graphics)
   implementation(libs.androidx.compose.ui.tooling.preview)
   implementation(libs.androidx.compose.ui.util)
   implementation(libs.androidx.compose.ui.test.junit4)
   implementation(libs.androidx.compose.material3)
   implementation(libs.androidx.lifecycle.compose)
   implementation(libs.coil)

   debugImplementation(libs.androidx.compose.ui.tooling)
}
