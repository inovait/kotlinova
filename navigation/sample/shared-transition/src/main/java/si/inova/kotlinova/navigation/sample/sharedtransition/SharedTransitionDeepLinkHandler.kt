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

package si.inova.kotlinova.navigation.sample.sharedtransition

import android.net.Uri
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import si.inova.kotlinova.navigation.deeplink.handleMultipleDeepLinks
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.instructions.ReplaceBackstackOrOpenScreen
import si.inova.kotlinova.navigation.sample.keys.MainScreenKey
import si.inova.kotlinova.navigation.sample.keys.SharedTransitionDetailsScreenKey
import si.inova.kotlinova.navigation.sample.keys.SharedTransitionListScreenKey

@ContributesIntoSet(OuterNavigationScope::class)
@Inject
class SharedTransitionDeepLinkHandler : DeepLinkHandler {
   override fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction? {
      return handleMultipleDeepLinks(uri, startup) {
         matchDeepLink("navigationdemo://colors") { _, startup ->
            ReplaceBackstackOrOpenScreen(startup, MainScreenKey, SharedTransitionListScreenKey)
         }
         matchDeepLink("navigationdemo://colors/{color}") { args, startup ->
            val colorIndex = args.getValue("color").toIntOrNull()
            if (colorIndex != null && colorIndex in SHARED_TRANSITION_COLORS.indices) {
               ReplaceBackstackOrOpenScreen(startup, MainScreenKey, SharedTransitionListScreenKey, SharedTransitionDetailsScreenKey(colorIndex))
            } else {
               null
            }
         }
      }
   }
}
