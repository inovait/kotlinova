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

package si.inova.kotlinova.navigation.simplestack

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.zhuinden.simplestack.AsyncStateChanger
import com.zhuinden.simplestack.Backstack
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

/**
 * Root composable that displays screens from navigation
 */
@SuppressLint("ComposableNaming") // Backstack return is only there as a convenience, it's mostly meant to be used without return
@Composable
fun NavigationInjection.Factory.RootNavigationContainer(
   screenWrapper: @Composable (key: ScreenKey, screen: @Composable () -> Unit) -> Unit = { _, screen -> screen() },
   initialHistory: () -> List<ScreenKey>
): Backstack {
   val composeStateChanger = remember { ComposeStateChanger() }
   val asyncStateChanger = remember(composeStateChanger) { AsyncStateChanger(composeStateChanger) }

   val backstack = this.rememberBackstack(asyncStateChanger) { initialHistory() }

   BackstackProvider(backstack) {
      composeStateChanger.Content(screenWrapper)
   }

   return backstack
}
