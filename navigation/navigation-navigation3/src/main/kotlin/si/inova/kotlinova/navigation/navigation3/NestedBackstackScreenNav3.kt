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

package si.inova.kotlinova.navigation.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.zhuinden.simplestack.Backstack
import kotlinx.android.parcel.Parcelize
import si.inova.kotlinova.navigation.di.MainNavigation
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.CurrentScopeTag
import si.inova.kotlinova.navigation.simplestack.LocalBackstack
import si.inova.kotlinova.navigation.simplestack.rememberBackstack

/**
 *
 */
@InjectNavigationScreen
open class NestedBackstackScreen(
   private val navigationStackComponentFactory: NavigationInjection.Factory,
   @MainNavigation
   private val mainBackstack: Backstack,
   @CurrentScopeTag
   val scope: String,
) : Screen<NestedNavigationScreenKey>() {
   @Composable
   override fun Content(key: NestedNavigationScreenKey) {
      val entryProvider = remember { Navigation3EntryProvider() }

      val parentBackstack = LocalBackstack.current

      navigationStackComponentFactory.rememberBackstack(
         entryProvider,
         id = key.id,
         initialHistory = { key.initialHistory },
         overrideMainBackstack = mainBackstack,
         parentBackstack = parentBackstack,
         parentBackstackScope = scope
      )

      NavDisplay(entryProvider)
   }

   @Composable
   protected open fun NavDisplay(
      navigation3EntryProvider: Navigation3EntryProvider,
   ) {
      navigation3EntryProvider.NavDisplay()
   }
}

@Parcelize
data class NestedNavigationScreenKey(
   val initialHistory: List<ScreenKey>,
   val id: String = "SINGLE",
) : ScreenKey()
