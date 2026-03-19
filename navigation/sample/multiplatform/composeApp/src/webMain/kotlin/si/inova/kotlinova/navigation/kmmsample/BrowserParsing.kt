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

package si.inova.kotlinova.navigation.kmmsample

import com.eygraber.uri.Uri
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.kmmsample.first.FirstScreenKey
import si.inova.kotlinova.navigation.kmmsample.second.SecondScreenKey
import si.inova.kotlinova.navigation.kmmsample.third.ThirdScreenKey
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

object MainBrowserUrlMatcher: BrowserUrlMatcher {
    override fun handleBrowserUrl(uri: Uri): List<WebScreenKey>? {
        return handleMultipleBrowserUrls(uri) {
            println("handle $uri")
            matchBrowserUrl("first") {
                listOf(FirstScreenKey)
            }

            matchBrowserUrl("second") {
                listOf(FirstScreenKey, SecondScreenKey)
            }

            matchBrowserUrl("third/{number}") {
                println("map $it")
                listOf(FirstScreenKey, SecondScreenKey, ThirdScreenKey(it.getValue("number").toIntOrNull() ?: 0))
            }
        }
    }

}

fun interface BrowserUrlMatcher {
    /**
     * Attempt to handle provided uri
     *
     * @param uri URI of the deep link
     * @param startup *true* if application was started with this link, *false* if application is already opened when deep link
     *                was triggered
     */
    fun handleBrowserUrl(uri: Uri): List<WebScreenKey>?
}

class MultiBrowserUrlMatcher : BrowserUrlMatcher {
    private val handlers = ArrayList<BrowserUrlMatcher>()

    /**
     * Attempt to match deep link with one of the provided patterns.
     * (See [Developer docs](https://developer.android.com/guide/navigation/navigation-deep-link#implicit)
     * for how to create valid patterns - this method uses same matching engine as AndroidX Navigation underneath).
     *
     * If link is matched, [mapper] is called with map of matched arguments. If you return non-null instructions from this
     * mapper, user will be navigated to those instructions.
     */
    fun matchBrowserUrl(vararg patterns: String, mapper: (args: Map<String, String>) -> List<WebScreenKey>?) {
        handlers += BrowserUrlMatcher { uri: Uri -> uri.matchBrowserUrl(*patterns) { mapper(it) } }
    }

    override fun handleBrowserUrl(uri: Uri): List<WebScreenKey>? {
        return handlers.asSequence().mapNotNull { it.handleBrowserUrl(uri) }.firstOrNull()
    }
}

@Suppress("UnusedReceiverParameter") // This parameter is used to limit autocomplete scope
fun BrowserUrlMatcher.handleMultipleBrowserUrls(
    uri: Uri,
    block: MultiBrowserUrlMatcher.() -> Unit,
): List<WebScreenKey>? {
    val multiBrowserUrlMatcher = MultiBrowserUrlMatcher()
    block(multiBrowserUrlMatcher)

    return multiBrowserUrlMatcher.
    handleBrowserUrl(uri)
}
