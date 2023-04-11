/*
 * Copyright 2023 INOVA IT d.o.o.
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

import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesSubcomponent
import com.squareup.anvil.annotations.ContributesTo
import com.zhuinden.simplestack.Backstack
import dagger.BindsInstance
import dagger.Module
import dagger.multibindings.Multibinds
import si.inova.kotlinova.navigation.conditions.ConditionalNavigationHandler
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import javax.inject.Inject

@ContributesSubcomponent(BackstackScope::class, parentScope = OuterNavigationScope::class)
interface NavigationStackComponent : NavigationInjection {

   @ContributesSubcomponent.Factory
   interface Factory : NavigationInjection.Factory {
      override fun create(
         @BindsInstance
         backstack: Backstack,
         @BindsInstance
         @MainNavigation
         mainBackstack: Backstack
      ): NavigationStackComponent
   }
}

@ContributesTo(OuterNavigationScope::class)
@Module()
abstract class NavigationStackComponentModule {
   @Multibinds
   abstract fun provideEmptyDeepLinkHandlersMultibinds(): Set<@JvmSuppressWildcards DeepLinkHandler>

   @Multibinds
   abstract fun provideEmptyConditionalNavigationHandlersMultibinds():
      Map<@JvmSuppressWildcards Class<*>, @JvmSuppressWildcards ConditionalNavigationHandler>
}

@ContributesBinding(OuterNavigationScope::class)
class FactoryImpl @Inject constructor(private val parentFactory: NavigationStackComponent.Factory) : NavigationInjection.Factory {
   override fun create(backstack: Backstack, mainBackstack: Backstack): NavigationInjection {
      return parentFactory.create(backstack, mainBackstack)
   }
}
