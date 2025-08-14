/*
 * Copyright 2025 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.kotlinova.navigation.di

import com.zhuinden.simplestack.Backstack
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesGraphExtension
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import si.inova.kotlinova.navigation.conditions.NavigationCondition
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import kotlin.reflect.KClass

@ContributesGraphExtension(BackstackScope::class)
interface NavigationStackSubGraph : NavigationInjection {
   @MainNavigation
   @Provides
   fun provideMainBackstack(mainBackstackWrapper: MainBackstackWrapper): Backstack = mainBackstackWrapper.backstack

   @ContributesGraphExtension.Factory(OuterNavigationScope::class)
   interface Factory {
      fun create(
         @Provides
         backstack: Backstack,
         @Provides
         mainBackstackWrapper: MainBackstackWrapper
      ): NavigationStackSubGraph
   }
}

class MainBackstackWrapper(val backstack: Backstack)

@ContributesTo(OuterNavigationScope::class)
interface NavigationStackProviders {
   @Multibinds(allowEmpty = true)
   fun getDeepLinkHandlers(): Set<DeepLinkHandler>

   @Multibinds(allowEmpty = true)
   fun getNavigationConditions(): Map<KClass<*>, NavigationCondition>
}

@ContributesBinding(OuterNavigationScope::class)
@Inject
class FactoryImpl(private val parentFactory: NavigationStackSubGraph.Factory) : NavigationInjection.Factory {
   override fun create(backstack: Backstack, mainBackstackWrapper: MainBackstackWrapper): NavigationInjection {
      return parentFactory.create(backstack, mainBackstackWrapper)
   }
}
