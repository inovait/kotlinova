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

package si.inova.kotlinova.navigation.sample.conditional

import android.net.Uri
import me.tatarka.inject.annotations.Inject
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import si.inova.kotlinova.navigation.deeplink.matchDeepLink
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.instructions.ClearBackstackAnd
import si.inova.kotlinova.navigation.instructions.NavigateWithConditions
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.instructions.OpenScreenOrMoveToTop
import si.inova.kotlinova.navigation.instructions.ReplaceBackstack
import si.inova.kotlinova.navigation.sample.keys.MainScreenKey
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding

@ContributesBinding(OuterNavigationScope::class, multibinding = true)
class PostLoginScreenDeepLinkHandler @Inject constructor() : DeepLinkHandler {
   override fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction? {
      return uri.matchDeepLink("navigationdemo://postlogin") {
         // Following code is intentionally verbose to demonstrate what actually happens behind the scenes.
         // We could replace entire following section with
         // `return ReplaceBackstackOrOpenScreen(startup, MainScreenKey, PostLoginScreenKey)`

         return if (startup) {
            // User has started the app from scratch via deep link. Recreate entire backstack

            // Using NavigationInstructions manually does not honor conditions by default. We have to make it explicit.
            // First we call NavigateWithConditions. Then:
            //    a) If condition is successful, it just calls the ReplaceBackstack
            //    b) Otherwise, it displays the login screen, via conditionScreenWrapper. Since this is on startup,
            //       we only want the login screen to display, without the root/main screen. That's why we wrap login call
            //       inside ClearBackstackAnd instruction.

            NavigateWithConditions(
               ReplaceBackstack(MainScreenKey, PostLoginScreenKey),
               LoginCondition,
               conditionScreenWrapper = { ClearBackstackAnd(it) }
            )
         } else {
            // User has triggered deep link when already inside the app. Just open the screen over existing backstack

            // Using NavigationInstructions manually does not honor conditions by default. We have to make it explicit.
            // We could also call it like this to avoid repetition of the LoginCondition
            // NavigateWithConditions(OpenScreen(PostLoginScreenKey), *PostLoginScreenKey.navigationConditions.toTypedArray())

            NavigateWithConditions(OpenScreenOrMoveToTop(PostLoginScreenKey), LoginCondition)
         }
      }
   }
}
