package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DISPOSED
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

fun MainViewController() = ComposeUIViewController {
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
