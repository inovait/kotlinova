/*
 * Copyright 2024 INOVA IT d.o.o.
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

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Parcel
import com.zhuinden.simplestack.Backstack
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import si.inova.kotlinova.navigation.conditions.ConditionalNavigationHandler
import si.inova.kotlinova.navigation.conditions.NavigationCondition
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.ContributesSubcomponent
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@SingleIn(BackstackScope::class)
@ContributesSubcomponent(BackstackScope::class)
interface NavigationStackSubComponent : NavigationInjection {
   @MainNavigation
   @Provides
   fun provideMainBackstack(mainBackstackWrapper: MainBackstackWrapper): Backstack = mainBackstackWrapper.backstack

   @ContributesSubcomponent.Factory(OuterNavigationScope::class)
   interface Factory : NavigationInjection.Factory {
      override fun create(
         backstack: Backstack,
         mainBackstackWrapper: MainBackstackWrapper
      ): NavigationStackSubComponent
   }
}

class MainBackstackWrapper(val backstack: Backstack)

@ContributesTo(OuterNavigationScope::class)
interface NavigationStackComponent {
   val deepLinkHandlers: Set<DeepLinkHandler>
   val conditionalNavigationHandlers: Map<Class<out NavigationCondition>, () -> ConditionalNavigationHandler>

   // Dummy registrations that will never be used but can be used to create multibindings.
   // Workaround for a lack of https://github.com/evant/kotlin-inject/issues/249

   @Provides
   @IntoSet
   fun provideDeepLinkHandler(): DeepLinkHandler = DummyDeepLinkHandler()

   @Provides
   @IntoMap
   fun provideConditionalNavigationHandler(): Pair<Class<out NavigationCondition>, () -> ConditionalNavigationHandler> =
      DummyDeepLinkHandler::class.java to { DummyDeepLinkHandler() }
}

@ContributesBinding(OuterNavigationScope::class)
class FactoryImpl @Inject constructor(private val parentFactory: NavigationStackSubComponent.Factory) :
   NavigationInjection.Factory {
   override fun create(backstack: Backstack, mainBackstackWrapper: MainBackstackWrapper): NavigationInjection {
      return parentFactory.create(backstack, mainBackstackWrapper)
   }
}

@SuppressLint("ParcelCreator")
private class DummyDeepLinkHandler : DeepLinkHandler, ConditionalNavigationHandler, NavigationCondition {
   override fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction? {
      return null
   }

   override fun getNavigationRedirect(
      condition: NavigationCondition,
      navigateToIfConditionMet: NavigationInstruction
   ): NavigationInstruction? {
      throw UnsupportedOperationException("This class should never be actually used")
   }

   override fun describeContents(): Int {
      throw UnsupportedOperationException("This class should never be actually used")
   }

   override fun writeToParcel(dest: Parcel, flags: Int) {
      throw UnsupportedOperationException("This class should never be actually used")
   }
}
