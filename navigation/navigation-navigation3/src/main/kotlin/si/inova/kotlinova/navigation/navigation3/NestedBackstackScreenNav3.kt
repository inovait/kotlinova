/*
 * Copyright 2026 INOVA IT d.o.o.
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

package si.inova.kotlinova.navigation.navigation3

import androidx.compose.runtime.Composable
import kotlinx.android.parcel.Parcelize
import si.inova.kotlinova.navigation.backstack.Backstack
import si.inova.kotlinova.navigation.backstack.LocalBackstack
import si.inova.kotlinova.navigation.backstack.rememberBackstack
import si.inova.kotlinova.navigation.di.MainNavigation
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.CurrentScopeTag

/**
 *
 */
@InjectNavigationScreen
class NestedBackstackScreen(
   navigationStackComponentFactory: NavigationInjection.Factory,
   @MainNavigation
   mainBackstack: Backstack,
   @CurrentScopeTag
   scope: String,
) : BaseNestedBackstackScreen<NestedNavigationScreenKey>(navigationStackComponentFactory, mainBackstack, scope)

abstract class BaseNestedBackstackScreen<K : BaseNestedNavigationScreenKey>(
   private val navigationStackComponentFactory: NavigationInjection.Factory,
   @MainNavigation
   private val mainBackstack: Backstack,
   @CurrentScopeTag
   val scope: String,
) : Screen<K>() {
   @Composable
   override fun Content(key: K) {
      val parentBackstack = LocalBackstack.current

      val backstack = navigationStackComponentFactory.rememberBackstack(
         id = key.id,
         initialHistory = { key.initialHistory },
         overrideMainBackstack = mainBackstack,
         parentBackstack = parentBackstack,
         parentBackstackScope = scope
      )

      val entryProvider = backstack.rememberNavigation3EntryProvider()

      NavDisplay(entryProvider)
   }

   @Composable
   protected open fun NavDisplay(
      navigation3EntryProvider: Navigation3EntryProvider,
   ) {
      navigation3EntryProvider.NavDisplay()
   }
}

abstract class BaseNestedNavigationScreenKey : ScreenKey() {
   abstract val initialHistory: List<ScreenKey>
   abstract val id: String
}

@Parcelize
data class NestedNavigationScreenKey(
   override val initialHistory: List<ScreenKey>,
   override val id: String = "SINGLE",
) : BaseNestedNavigationScreenKey()
