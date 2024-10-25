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

// https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("DSL_SCOPE_VIOLATION")

import com.android.build.gradle.BaseExtension

buildscript {
   repositories {
      google()
      mavenCentral()
      gradlePluginPortal()
   }

   dependencies {
      classpath(libs.android.agp)
      classpath(libs.kotlin.plugin)
      classpath(libs.kotlinInject.compiler)
      classpath(libs.kotlinInject.anvil.compiler)
      classpath(libs.ksp)
   }
}

subprojects {
   afterEvaluate {
      extensions.configure<BaseExtension>("android") {
         compileOptions {
            isCoreLibraryDesugaringEnabled = true
         }
      }

      dependencies {
         add("coreLibraryDesugaring", libs.desugarJdkLibs)
      }
   }

   configurations.all {
      resolutionStrategy {
         force("com.squareup.anvil:annotations-optional:2.5.0-beta09")
      }
   }
}
