import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer?.apply {
                    proxy = mutableListOf(
                        KotlinWebpackConfig.DevServer.Proxy(
                            target = "http://localhost:8080/",
                            pathRewrite = mutableMapOf(".*" to "http://localhost:8080/"),
                            context = mutableListOf("/app"),
                            changeOrigin = true
                        )
                    )

                    open = false
                }
            }
        }

        binaries.executable()
    }

    sourceSets {
        webMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.savedstate)
            implementation(libs.uriKmp)
        }
    }
}
