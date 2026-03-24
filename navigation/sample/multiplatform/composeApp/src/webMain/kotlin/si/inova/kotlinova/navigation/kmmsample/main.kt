package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.jetbrains.compose.resources.configureWebResources

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { "/$it" }
    }


    ComposeViewport {
        val browserNavigation = rememberAndLinkBrowserNavigationToKotlinovaNavigation(
            "/app/",
            MainBrowserUrlMatcher,
        )

        val viewModelStoreOwner = remember { MyViewModelStoreOwner() }
        val saveableStateRegistry = remember { BrowserSaveableStateRegistry() }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner,
            LocalSaveableStateRegistry provides saveableStateRegistry,
        ) {
            App(
                initialHistory = browserNavigation::getInitialBackstackHistory,
                onBackstackReady = browserNavigation::onBackstackInitialized
            )
        }

        val browserWindow = refBrowserWindow()

        LaunchedEffect(Unit) {
            browserWindow.visibilityChangeEvent().collect {
                // Save state whenever the window goes into the background
                // This allows us to restore the state if/when tab is killed and restored
                saveableStateRegistry.save()
            }
        }

        LaunchedEffect(Unit) {
            browserWindow.unloadEvent().collect {
                viewModelStoreOwner.viewModelStore.clear()
            }
        }
    }
}
