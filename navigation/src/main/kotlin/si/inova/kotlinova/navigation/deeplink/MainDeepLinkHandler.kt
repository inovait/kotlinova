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

package si.inova.kotlinova.navigation.deeplink

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer
import com.zhuinden.simplestack.Backstack
import me.tatarka.inject.annotations.Inject
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.simplestack.RootNavigationContainer

/**
 * Deep link handler that aggregates all deep link handlers contributed via `@ContributesMultibinding`.
 */
class MainDeepLinkHandler @Inject constructor(
   private val injectedHandlers: Set<DeepLinkHandler>
) : DeepLinkHandler {
   override fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction? {
      return injectedHandlers.asSequence().mapNotNull { it.handleDeepLink(uri, startup) }.firstOrNull()
   }
}

/**
 * Automatically handle all deep links that arrive after activity has been created (ones that would usually
 * be routed to the [Activity.onNewIntent]).
 *
 * @param activity Activity that receives the links
 * @param backstack backstack that you receive from [RootNavigationContainer]
 */
@Composable
fun MainDeepLinkHandler.HandleNewIntentDeepLinks(activity: ComponentActivity, backstack: Backstack) {
   DisposableEffect(this, activity, backstack) {
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      val newIntentListener = Consumer<Intent> { intent ->
         intent.data?.let { deepLink ->
            handleDeepLink(deepLink, startup = false)?.let {
               navigator.navigate(it)
            }
         }
      }

      activity.addOnNewIntentListener(newIntentListener)

      onDispose {
         activity.removeOnNewIntentListener(newIntentListener)
      }
   }
}
