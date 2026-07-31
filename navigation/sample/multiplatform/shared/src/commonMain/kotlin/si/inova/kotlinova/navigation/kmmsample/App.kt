package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import si.inova.kotlinova.navigation.backstack.Backstack
import si.inova.kotlinova.navigation.kmmsample.first.FirstScreenKey
import si.inova.kotlinova.navigation.navigation3.NavDisplay
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Composable
@Preview
fun App(
    initialHistory: () -> List<ScreenKey> = { DEFAULT_INITIAL_HISTORY },
    onBackstackReady: (Backstack) -> Unit = {}
) {
    MaterialTheme {
        val backstack = loadNavigationInjectionFactory().NavDisplay(
            initialHistory = initialHistory,
        )

        onBackstackReady(backstack)
    }
}

val DEFAULT_INITIAL_HISTORY = listOf(FirstScreenKey)
