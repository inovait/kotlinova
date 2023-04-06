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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import util.publishLibrary

plugins {
   id("standard-config")
   id("java-library")
   id("org.jetbrains.kotlin.jvm")
   id("kotlin-kapt")
}

publishLibrary(
   userFriendlyName = "Navigation compiler",
   description = "Anvil compiler for navigation",
   githubPath = "navigation-compiler"
)

publishing {
   publications {
      create<MavenPublication>("maven") {
         groupId = project.group.toString()
         artifactId = "navigation-compiler"
         version = project.version.toString()

         from(components["java"])
      }
   }
}

java {
   withJavadocJar()
   withSourcesJar()

   sourceCompatibility = JavaVersion.VERSION_11
   targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
   api(libs.anvil.api)
   implementation(libs.anvil.utils)
   implementation(libs.dagger.runtime)
   compileOnly(libs.autoService.annotations)
   kapt(libs.autoService.compiler)
}
