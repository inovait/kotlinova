import util.publishLibrary

plugins {
   androidLibraryModule
   id("kotlin-parcelize")
}

publishLibrary(
   userFriendlyName = "Compose",
   description = "Set of utilities for Jetpack Compose",
   githubPath = "compose"
)

android {
   namespace = "si.inova.kotlinova.compose"

   buildFeatures {
      compose = true
   }
   composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
   }
}

dependencies {
   implementation(projects.kotlinova.core)

   implementation(libs.androidx.compose.ui)
   implementation(libs.androidx.compose.ui.graphics)
   implementation(libs.androidx.compose.ui.tooling.preview)
   implementation(libs.androidx.compose.ui.util)
   implementation(libs.androidx.compose.material3)
   implementation(libs.androidx.lifecycle.compose)
   implementation(libs.coil)

   debugImplementation(libs.androidx.compose.ui.tooling)
}
