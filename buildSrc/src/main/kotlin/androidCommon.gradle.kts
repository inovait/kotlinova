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

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.tasks.asJavaVersion
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import util.COMMON_COMPILE_SDK
import util.COMMON_MIN_SDK
import util.commonAndroid

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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

val libs = the<LibrariesForLibs>()

plugins {
   id("standardConfig")
}

if (project.pluginManager.hasPlugin("com.android.kotlin.multiplatform.library")) {
   val multiplatformExtension = extensions.getByType<KotlinMultiplatformExtension>()
   multiplatformExtension.extensions.configure<KotlinMultiplatformAndroidLibraryTarget>() {
      compileSdk = COMMON_COMPILE_SDK
      minSdk = COMMON_MIN_SDK
   }
} else {
   commonAndroid {
      compileSdk = 36

      compileOptions.apply {
         // Android still creates java tasks, even with 100% Kotlin.
         // Ensure that target compatiblity is equal to kotlin's jvmToolchain
         lateinit var javaVersion: JavaVersion
         project.the<KotlinAndroidProjectExtension>().jvmToolchain { javaVersion = this.languageVersion.get().asJavaVersion() }
         targetCompatibility = javaVersion
         isCoreLibraryDesugaringEnabled = true
      }

      defaultConfig.apply {
         minSdk = 23

         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      }

      testOptions.apply {
         unitTests.all {
            it.useJUnitPlatform()

            // Better test output
            it.systemProperty("kotest.assertions.collection.print.size", "300")
            it.systemProperty("kotest.assertions.collection.enumerate.size", "300")
         }
      }

      packaging.apply {
         resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
         }
      }

      lint.apply {
         lintConfig = file("$rootDir/config/android-lint.xml")
         abortOnError = true

         warningsAsErrors = true
      }

      buildTypes.apply {
         getByName("debug") {
            testCoverage.apply {
               jacocoVersion = libs.versions.jacoco.get()
            }
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
         }
      }
   }
}
dependencies {
   add("coreLibraryDesugaring", libs.desugarJdkLibs)
}
