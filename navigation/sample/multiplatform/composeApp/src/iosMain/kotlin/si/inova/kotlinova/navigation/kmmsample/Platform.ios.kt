package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice
import si.inova.kotlinova.navigation.di.NavigationInjection

class IOSPlatform : Platform {
   override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@Composable
actual fun loadNavigationInjectionFactory(): NavigationInjection.Factory {
   TODO("Not yet implemented")
}
