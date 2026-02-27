package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import si.inova.kotlinova.navigation.kmmsample.first.FirstScreenKey
import si.inova.kotlinova.navigation.navigation3.NavDisplay

@Composable
@Preview
fun App() {
   MaterialTheme {
      loadNavigationInjectionFactory().NavDisplay(
         initialHistory = { listOf(FirstScreenKey) }
      )
   }
}
