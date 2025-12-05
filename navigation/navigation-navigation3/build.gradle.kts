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
   id("com.google.devtools.ksp")
   id("org.jetbrains.kotlin.plugin.compose")
   alias(libs.plugins.metro)
}

publishLibrary(
   userFriendlyName = "Navigation Navigation3",
   description = "Module that enables integration of the Kotlinova Navigation with the Google's Navigation3 library",
   githubPath = "navigation"
)

android {
   namespace = "si.inova.kotlinova.navigation.navigation3"
}

dependencies {
   api(projects.navigation)
   api(libs.androidx.navigation3)
   api(libs.androidx.navigation3.ui)

   implementation(libs.androidx.compose.ui)

   ksp(projects.navigation.navigationCompiler)

   debugImplementation(libs.androidx.compose.ui.tooling)
}
