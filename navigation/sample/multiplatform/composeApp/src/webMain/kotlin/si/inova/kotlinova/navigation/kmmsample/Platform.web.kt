package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.OuterNavigationScope

class WebPlatform : Platform {
    override val name: String = "Web"
}

actual fun getPlatform(): Platform = WebPlatform()

@Composable
actual fun loadNavigationInjectionFactory(): NavigationInjection.Factory {
    return remember {
        DependencyInjectionHolder.appGraph.getNavigationInjectionFactory()
    }
}

object DependencyInjectionHolder {
    val appGraph by lazy { createGraph<WebAppGraph>() }
}

@DependencyGraph(AppScope::class, additionalScopes = [OuterNavigationScope::class])
@SingleIn(AppScope::class)
interface WebAppGraph: AppGraph {
    override fun getNavigationInjectionFactory(): NavigationInjection.Factory
}
