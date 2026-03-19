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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.eygraber.uri.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import si.inova.kotlinova.navigation.backstack.Backstack
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberAndLinkBrowserNavigationToKotlinovaNavigation(
    appPathPrefix: String,
    urlMatcher: BrowserUrlMatcher,
): BrowserNavigationMethods {
    val scope = rememberCoroutineScope()
    val state = remember(appPathPrefix, urlMatcher, scope) {
        BrowserNavigationState(appPathPrefix, urlMatcher, scope)
    }

    return state
}

private class BrowserNavigationState(
    private val appPathPrefix: String,
    private val urlMatcher: BrowserUrlMatcher,
    private val coroutineScope: CoroutineScope,
) : BrowserNavigationMethods {
    private var backstack: Backstack? = null

    private val browserWindow = refBrowserWindow()

    override fun onBackstackInitialized(backstack: Backstack) {
        this.backstack = backstack

        val relativePath = browserWindow.location.pathname.removePrefix(appPathPrefix)
        if ((backstack.backstack.value.last() as WebScreenKey).getPathLink() != relativePath) {
            val newBackstack = calculateAndInsertBackstackIntoBrowserHistory(relativePath)
            backstack.updateBackstack(newBackstack)
        }

        startListeners()
    }

    override fun getInitialBackstackHistory(): List<ScreenKey> {
        val relativePath = browserWindow.location.pathname.removePrefix(appPathPrefix)
        return calculateAndInsertBackstackIntoBrowserHistory(relativePath)
    }

    private fun calculateAndInsertBackstackIntoBrowserHistory(relativePath: String): List<ScreenKey> {
        val newBackstack = getBackstackForUri(relativePath) ?: DEFAULT_INITIAL_HISTORY
        println("NB $newBackstack")

        coroutineScope.launch {
            val backstackAtFirstItem = newBackstack.take(1)
            val (stateAtFirstItem, topUrlOfFirstItem) = calculateBrowserHistoryState(backstackAtFirstItem)
            browserWindow.history.replaceState(stateAtFirstItem, "", topUrlOfFirstItem)
            println("replace first $stateAtFirstItem $topUrlOfFirstItem")

            for (i in 2..newBackstack.size) {
                // Firefox goes into weird state if you call pushState twice in a row quickly. Do it with delay.
                delay(500.milliseconds)

                val backstackAtThisPoint = newBackstack.take(i)
                val (stateAtThisPoint, topUrlAtThisPoint) = calculateBrowserHistoryState(backstackAtThisPoint)
                browserWindow.history.pushState(stateAtThisPoint, "", topUrlAtThisPoint)
                println("pushState $stateAtThisPoint $topUrlAtThisPoint")
            }
        }

        @Suppress("UNCHECKED_CAST")
        return newBackstack as List<ScreenKey>


    }

    private fun startListeners() {
        handleBrowserPopEvents()
        handleBackstackChanges()
    }

    private fun handleBrowserPopEvents() {
        coroutineScope.launch {
            browserWindow.popState().collect {
                val state = it.state ?: return@collect

                val newHistory = state.split("/")
                    .map {
                        val screenUri = decodeURIComponent(it)
                        urlMatcher.handleBrowserUrl(Uri.parse(screenUri))?.last()
                            ?: error("Unknown screen key uri '$screenUri'")
                    }

                @Suppress("UNCHECKED_CAST")
                backstack?.updateBackstack(newHistory as List<ScreenKey>)
            }
        }
    }

    private fun handleBackstackChanges() {
        coroutineScope.launch {
            val backstack = requireNotNull(backstack)

            backstack.backstack.collect { stack ->
                val keys = stack.map { it as WebScreenKey }

                val (newState, newTopUrl) = calculateBrowserHistoryState(keys)
                val currentState = browserWindow.history.state

                if (currentState == newState) {
                    return@collect
                }
                if (currentState == null) {
                    browserWindow.history.replaceState(newState, "", newTopUrl)
                } else {
                    browserWindow.history.pushState(newState, "", newTopUrl)
                }
            }
        }
    }

    private fun calculateBrowserHistoryState(
        keys: List<WebScreenKey>,
    ): Pair<String, String> {
        val newState = keys.joinToString("/") { encodeURIComponent(it.getPathLink()) }
        val newTopUrl = "$appPathPrefix${keys.last().getPathLink()}"
        return Pair(newState, newTopUrl)
    }

    private fun getBackstackForUri(uri: String): List<WebScreenKey>? {
        val backstack = urlMatcher.handleBrowserUrl(Uri.parse(uri))
        if (backstack == null) {
            println("Unknown backstack URI: $uri")
        }

        return backstack
    }
}

interface BrowserNavigationMethods {
    fun onBackstackInitialized(backstack: Backstack)
    fun getInitialBackstackHistory(): List<ScreenKey>
}
