package org.example.project

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.jetbrains.compose.resources.configureWebResources
import si.inova.kotlinova.navigation.kmmsample.App
import si.inova.kotlinova.navigation.kmmsample.MyViewModelStoreOwner

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { "/$it" }
    }
    ComposeViewport {
        val viewModelStoreOwner = remember { MyViewModelStoreOwner() }

        DisposableEffect(viewModelStoreOwner) {
            onDispose {
                viewModelStoreOwner.viewModelStore.clear()
            }
        }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner
        ) {
            App()
        }

    }
}
