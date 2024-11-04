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
   id("com.google.devtools.ksp")
   id("org.jetbrains.kotlin.plugin.compose")
}

publishLibrary(
   userFriendlyName = "Navigation fragment",
   description = "Module that enables navigating to fragments as navigation destinations",
   githubPath = "navigation"
)

android {
   namespace = "si.inova.kotlinova.navigation.fragment"
}

dependencies {
   api(libs.kotlinInject.runtime)
   api(libs.kotlinInject.anvil.runtime)

   implementation(projects.navigation)

   implementation(libs.androidx.fragment)
   implementation(libs.androidx.compose.ui)

   ksp(libs.kotlinInject.compiler)
   ksp(libs.kotlinInject.anvil.compiler)
   ksp(projects.navigation.navigationCompiler)

   debugImplementation(libs.androidx.compose.ui.tooling)
}
