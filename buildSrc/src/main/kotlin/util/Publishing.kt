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

package util

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign

fun Project.publishLibrary(
   userFriendlyName: String,
   description: String,
   githubPath: String,
   artifactName: String = project.name
) {
   setProjectMetadata(userFriendlyName, description, githubPath)
   configureForMavenCentral()
   forceArtifactName(artifactName)
}

private fun Project.setProjectMetadata(
   userFriendlyName: String,
   description: String,
   githubPath: String
) {
   extensions.configure<org.gradle.api.publish.PublishingExtension>("publishing") {
      publications.withType<MavenPublication> {
         pom {
            name.set(userFriendlyName)
            this.description.set(description)
            val projectGitUrl = "https://github.com/inovait/kotlinova"
            url.set("$projectGitUrl/tree/master/$githubPath")
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
            developers {
               developer {
                  name.set("Inova IT")
                  url.set("https://inova.si/")
               }
            }
         }
      }
   }
}

private fun Project.configureForMavenCentral() {
   if (properties.containsKey("ossrhUsername")) {
      extensions.configure<org.gradle.plugins.signing.SigningExtension>("signing") {
         sign(extensions.getByName<org.gradle.api.publish.PublishingExtension>("publishing").publications)
      }

      // Workaround for the https://github.com/gradle/gradle/issues/26091
      tasks.withType<AbstractPublishToMaven>().configureEach {
         val signingTasks = tasks.withType<Sign>()
         mustRunAfter(signingTasks)
      }

      extensions.configure<org.gradle.api.publish.PublishingExtension>("publishing") {
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
}

private fun Project.forceArtifactName(artifactName: String) {
   afterEvaluate {
      extensions.configure<org.gradle.api.publish.PublishingExtension>("publishing") {
         publications.withType<MavenPublication> {
            artifactId = artifactId.replace(project.name, artifactName)
         }
      }
   }
}
