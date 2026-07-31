package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.NavigationSerializersModule
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.kmmsample.browsernavigation.WebNavigator
import si.inova.kotlinova.navigation.kmmsample.first.FirstScreenKey
import si.inova.kotlinova.navigation.kmmsample.second.SecondScreenKey
import si.inova.kotlinova.navigation.kmmsample.third.ThirdScreenKey
import si.inova.kotlinova.navigation.navigator.DefaultNavigator
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.serialization.defaultNavigationSerializersModule

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

@DependencyGraph(
    AppScope::class,
    additionalScopes = [OuterNavigationScope::class],
    excludes = [DefaultNavigator::class]
)
@SingleIn(AppScope::class)
interface WebAppGraph : AppGraph {
    override fun getNavigationInjectionFactory(): NavigationInjection.Factory

    @NavigationSerializersModule
    fun getNavigationSavedStateJsonSerializer(): Json


    @NavigationSerializersModule
    @Provides
    @SingleIn(AppScope::class)
    fun provideNavigationSavedStateJsonSerializer(
        @NavigationSerializersModule
        serializersModule: SerializersModule
    ): Json {
     return Json {
         this.serializersModule = serializersModule
     }
    }
}

@ContributesTo(OuterNavigationScope::class)
interface NavigationProviders {
    @Provides
    @NavigationSerializersModule
    fun provideNavigationSerializers(): SerializersModule = SerializersModule {
        include(defaultNavigationSerializersModule)

        polymorphic(ScreenKey::class) {
            subclass(FirstScreenKey::class)
            subclass(SecondScreenKey::class)
            subclass(ThirdScreenKey::class)
        }

        contextual(ScreenKey::class, PolymorphicSerializer<ScreenKey>(ScreenKey::class))
    }

    @Provides
    fun provideWebNavigator(defaultNavigator: DefaultNavigator): Navigator {
        return WebNavigator(defaultNavigator)
    }
}
