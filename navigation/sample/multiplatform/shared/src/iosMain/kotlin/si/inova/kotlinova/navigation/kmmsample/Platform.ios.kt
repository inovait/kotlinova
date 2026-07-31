package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import platform.UIKit.UIDevice
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.OuterNavigationScope

class IOSPlatform : Platform {
   override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@Composable
actual fun loadNavigationInjectionFactory(): NavigationInjection.Factory {
   return remember {
      DependencyInjectionHolder.appGraph.getNavigationInjectionFactory()
   }
}

object DependencyInjectionHolder {
   val appGraph by lazy { createGraph<iOSAppGraph>() }
}

@DependencyGraph(AppScope::class, additionalScopes = [OuterNavigationScope::class])
@SingleIn(AppScope::class)
interface iOSAppGraph: AppGraph {
   override fun getNavigationInjectionFactory(): NavigationInjection.Factory
}
