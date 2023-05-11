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

package si.inova.kotlinova.navigation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.zhuinden.simplestack.AsyncStateChanger
import com.zhuinden.simplestack.Backstack
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.di.MainNavigation
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.services.CurrentScopeTag
import si.inova.kotlinova.navigation.simplestack.BackstackProvider
import si.inova.kotlinova.navigation.simplestack.ComposeStateChanger
import si.inova.kotlinova.navigation.simplestack.LocalBackstack
import si.inova.kotlinova.navigation.simplestack.rememberBackstack

/**
 *
 */
class NestedBackstackScreen(
   private val navigationStackComponentFactory: NavigationInjection.Factory,
   @MainNavigation
   private val mainBackstack: Backstack,
   @CurrentScopeTag
   val scope: String
) : Screen<NestedNavigationScreenKey>() {
   @Composable
   override fun Content(key: NestedNavigationScreenKey) {
      val composeStateChanger = remember { ComposeStateChanger() }
      val asyncStateChanger = remember(composeStateChanger) { AsyncStateChanger(composeStateChanger) }

      val parentBackstack = LocalBackstack.current

      val backstack = navigationStackComponentFactory.rememberBackstack(
         asyncStateChanger,
         id = key.id,
         initialHistory = { key.initialHistory },
         interceptBackButton = key.interceptBackButton,
         overrideMainBackstack = mainBackstack,
         parentBackstack = parentBackstack,
         parentBackstackScope = scope
      )

      BackstackProvider(backstack) {
         composeStateChanger.Content()
      }
   }
}

@Parcelize
data class NestedNavigationScreenKey(
   val initialHistory: List<ScreenKey>,
   val id: String = "SINGLE",
   val interceptBackButton: Boolean = true
) : ScreenKey()
