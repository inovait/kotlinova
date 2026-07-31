package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.runtime.Composable
import si.inova.kotlinova.navigation.di.NavigationInjection

interface Platform {
   val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun loadNavigationInjectionFactory(): NavigationInjection.Factory
