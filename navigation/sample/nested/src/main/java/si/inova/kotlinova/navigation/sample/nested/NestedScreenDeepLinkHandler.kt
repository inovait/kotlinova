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

package si.inova.kotlinova.navigation.sample.nested

import android.net.Uri
import com.squareup.anvil.annotations.ContributesMultibinding
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import si.inova.kotlinova.navigation.deeplink.matchDeepLink
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.instructions.OpenScreenOrMoveToTop
import si.inova.kotlinova.navigation.instructions.ReplaceBackstack
import si.inova.kotlinova.navigation.sample.keys.MainScreenKey
import si.inova.kotlinova.navigation.sample.keys.NestedScreenKey
import javax.inject.Inject

@ContributesMultibinding(OuterNavigationScope::class)
class NestedScreenDeepLinkHandler @Inject constructor() : DeepLinkHandler {
   override fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction? {
      return uri.matchDeepLink("navigationdemo://nested") {
         // Following code is intentionally verbose to demonstrate what actually happens behind the scenes.
         // We could replace entire following section with
         // `return ReplaceBackstackOrOpenScreen(startup, MainScreenKey, NestedScreenKey`

         return if (startup) {
            // User has started the app from scratch via deep link. Recreate entire backstack
            ReplaceBackstack(MainScreenKey, NestedScreenKey)
         } else {
            // User has triggered deep link when already inside the app. Just open the screen over existing backstack
            // Use OpenScreenOrMoveToTop to ensure screens do not duplicate
            OpenScreenOrMoveToTop(NestedScreenKey)
         }
      }
   }
}
