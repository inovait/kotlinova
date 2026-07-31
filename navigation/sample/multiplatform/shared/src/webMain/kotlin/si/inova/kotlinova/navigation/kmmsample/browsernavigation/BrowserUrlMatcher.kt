package si.inova.kotlinova.navigation.kmmsample.browsernavigation

import com.eygraber.uri.Uri
import si.inova.kotlinova.navigation.kmmsample.WebScreenKey

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
