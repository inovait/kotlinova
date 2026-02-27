import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
   alias(libs.plugins.kotlinMultiplatform)
   alias(libs.plugins.kotlinSerialization)
   alias(libs.plugins.androidApplication)
   alias(libs.plugins.composeMultiplatform)
   alias(libs.plugins.composeCompiler)
   alias(libs.plugins.ksp)
   alias(libs.plugins.metro)
}

kotlin {
   androidTarget {
      compilerOptions {
         jvmTarget.set(JvmTarget.JVM_11)
      }
   }

   listOf(
      iosArm64(),
   ).forEach { iosTarget ->
      iosTarget.binaries.framework {
         baseName = "ComposeApp"
         isStatic = true
      }
   }

   sourceSets {
      androidMain.dependencies {
         implementation(libs.compose.uiToolingPreview)
         implementation(libs.androidx.activity.compose)
      }
      commonMain.dependencies {
         implementation(libs.compose.runtime)
         implementation(libs.compose.foundation)
         implementation(libs.compose.material3)
         implementation(libs.compose.ui)
         implementation(libs.compose.components.resources)
         implementation(libs.compose.uiToolingPreview)
         implementation(libs.androidx.lifecycle.viewmodelCompose)
         implementation(libs.androidx.lifecycle.runtimeCompose)
         implementation(libs.kotlin.serialization)
         implementation(libs.kotlinova.navigation)
         implementation(libs.kotlinova.navigation.navigation3)
      }
      commonTest.dependencies {
         implementation(libs.kotlin.test)
      }
   }
}

dependencies {
   ksp(libs.kotlinova.navigation.compiler)
}

android {
   namespace = "si.inova.kotlinova.navigation.kmmsample"
   compileSdk = libs.versions.android.compileSdk.get().toInt()

   defaultConfig {
      applicationId = "si.inova.kotlinova.navigation.kmmsample"
      minSdk = libs.versions.android.minSdk.get().toInt()
      targetSdk = libs.versions.android.targetSdk.get().toInt()
      versionCode = 1
      versionName = "1.0"
   }
   packaging {
      resources {
         excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
   }
   buildTypes {
      getByName("release") {
         isMinifyEnabled = false
      }
   }
   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11

      isCoreLibraryDesugaringEnabled = true
   }
}

dependencies {
   debugImplementation(libs.compose.uiTooling)
   coreLibraryDesugaring(libs.desugarJdkLibs)
}

