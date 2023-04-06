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

package si.inova.kotlinova.navigation.deeplink

import android.net.Uri
import si.inova.kotlinova.navigation.instructions.NavigationInstruction

fun interface DeepLinkHandler {
   /**
    * Attempt to handle provided uri
    *
    * @param uri URI of the deep link
    * @param startup *true* if application was started with this link, *false* if application is already opened when deep link
    *                was triggered
    */
   fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction?
}

class MultiDeepLinkHandler : DeepLinkHandler {
   private val handlers = ArrayList<DeepLinkHandler>()

   /**
    * Attempt to match deep link with one of the provided patterns.
    * (See [Developer docs](https://developer.android.com/guide/navigation/navigation-deep-link#implicit)
    * for how to create valid patterns - this method uses same matching engine as AndroidX Navigation underneath).
    *
    * If link is matched, [mapper] is called with map of matched arguments. If you return non-null instructions from this
    * mapper, user will be navigated to those instructions.
    */
   fun matchDeepLink(vararg patterns: String, mapper: (args: Map<String, String>, startup: Boolean) -> NavigationInstruction?) {
      handlers += DeepLinkHandler { uri: Uri, startup: Boolean -> uri.matchDeepLink(*patterns) { mapper(it, startup) } }
   }

   /**
    * Attempt to match a deep link with a custom lambda. If you return non-null instructions from the [block],
    * user will be navigated to those instructions.
    */
   fun customHandler(block: DeepLinkHandler) {
      handlers += block
   }

   override fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction? {
      return handlers.asSequence().mapNotNull { it.handleDeepLink(uri, startup) }.firstOrNull()
   }
}

/**
 * Handle multiple deep links in single deep link handler. Forward both uri and startup parameters from the parent handler to
 * this call.
 *
 * Handler will move linearly through all inner handlers, stopping whenever a match is found. It will return *null* if none
 * of the inner handlers can handle the deep link.
 *
 * Example use:
 *
 * ```kotlin
 * class MyDeepLinkHandler : DeepLinkHandler {
 *   override fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction? {
 *     return handleMultipleDeepLinks(uri, startup) {
 *        matchDeepLink("...") { args, startup ->
 *          ...
 *        }
 *        matchDeepLink("...") { args, startup ->
 *          ...
 *        }
 *     }
 *  }

 */
@Suppress("UnusedReceiverParameter") // This parameter is used to limit autocomplete scope
fun DeepLinkHandler.handleMultipleDeepLinks(
   uri: Uri,
   startup: Boolean,
   block: MultiDeepLinkHandler.() -> Unit
): NavigationInstruction? {
   val multiDeepLinkHandler = MultiDeepLinkHandler()
   block(multiDeepLinkHandler)

   return multiDeepLinkHandler.handleDeepLink(uri, startup)
}
