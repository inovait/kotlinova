import org.jetbrains.dokka.gradle.DokkaTask

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

plugins {
   id("checks")
   signing

   id("maven-publish")
   id("com.squareup.anvil")
   id("org.jetbrains.dokka")
}

anvil {
   generateDaggerFactories.set(true)
}

group = "si.inova.kotlinova"
version = File(rootDir, "version.txt").readText().trim()


tasks.getByName("dokkaJavadoc").also {
   println("javadocType: ${it.javaClass}")
}

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
   val dokkaJavadocTask = tasks.getByName("dokkaJavadoc", DokkaTask::class)
   dependsOn(dokkaJavadocTask)
   archiveClassifier.set("javadoc")
   from(dokkaJavadocTask.outputDirectory)
}

publishing {
   publications.withType<MavenPublication> {
      artifact(javadocJar)

      pom {
         val projectGitUrl = "https://github.com/inovait/kotlinova"
         url.set(projectGitUrl)
         inceptionYear.set("2023")
         licenses {
            license {
               name.set("MIT")
               url.set("https://opensource.org/licenses/MIT")
            }
         }
         issueManagement {
            system.set("GitHub")
            url.set("$projectGitUrl/issues")
         }
         scm {
            connection.set("scm:git:$projectGitUrl")
            developerConnection.set("scm:git:$projectGitUrl")
            url.set(projectGitUrl)
         }
      }
   }
}

if (properties.containsKey("ossrhUsername")) {
   signing {
      sign(publishing.publications)
   }

   publishing {
      repositories {
         maven {
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
               username = property("ossrhUsername") as String
               password = property("ossrhPassword") as String
            }
         }
      }
   }
}
