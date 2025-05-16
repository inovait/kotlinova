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

import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.gradle.accessors.dm.LibrariesForLibs

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
   id("com.android.library")
   kotlin("android")
   id("android-commons")
   id("kotlin-parcelize")
}

android {
   testOptions {
      unitTests.all {
         it.useJUnitPlatform()
      }
   }
}

mavenPublishing {
   configure(
      AndroidSingleVariantLibrary(
         variant = "release",
         sourcesJar = true,
         publishJavadocJar = false
      )
   )
}

// We cannot reuse empty-javadoc.jar over different projects, because that breaks Gradle's project isolation.
// Instead, we copy empty javadoc to project's build folder and then use this one
val dummyJavadocFolder = File(project.layout.buildDirectory.asFile.get(), "emptyJavadoc").also { it.mkdirs() }
val copyJavadocTask = tasks.register<Copy>("copyJavadoc") {
   from("${rootProject.rootDir}/config/empty-javadoc.jar")
   into(dummyJavadocFolder)
}

afterEvaluate {
   tasks.withType<Sign>().configureEach {
      dependsOn(copyJavadocTask)
   }
   tasks.withType<AbstractPublishToMaven>().configureEach {
      dependsOn(copyJavadocTask)
   }
}

publishing {
   publications {
      register<MavenPublication>("release") {
         // Add empty javadoc until https://github.com/Kotlin/dokka/issues/2956 is resolved
         artifact("$dummyJavadocFolder/empty-javadoc.jar") {
            classifier = "javadoc"
         }
      }
   }
}
