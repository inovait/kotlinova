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
   `kotlin-dsl`
   alias(libs.plugins.detekt)
   // We must specify JVM plugin explicitly here to avoid version conflicts
   // It produces "Unsupported Kotlin plugin version" but it lets us compile
   // See https://slack-chats.kotlinlang.org/t/29177439/when-updating-kotlin-to-2-2-0-i-m-getting-https-github-com-t
   alias(libs.plugins.kotlin.jvm)
}

repositories {
   google()
   mavenCentral()
   gradlePluginPortal()
}

detekt {
   config.from(files("$projectDir/../config/detekt.yml", "$projectDir/../config/detekt-buildSrc.yml"))
}

dependencies {

   implementation(libs.androidGradleCacheFix)
   implementation(libs.android.agp)
   implementation(libs.ksp)
   implementation(libs.detekt.plugin)
   implementation(libs.dokka)
   implementation(libs.kotlin.plugin)
   implementation(libs.kotlin.plugin.compose)
   implementation(libs.mavenPublish)
   implementation("si.inova.kotlinova:kotlinova-gradle")

   // Workaround to have libs accessible (from https://github.com/gradle/gradle/issues/15383)
   compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

   detektPlugins(libs.detekt.ktlint)
}

tasks.register("pre-commit-hook", Copy::class) {
   from("$rootDir/../config/hooks/")
   into("$rootDir/../.git/hooks")
}

afterEvaluate {
   tasks.getByName("jar").dependsOn("pre-commit-hook")
}
