package si.inova.kotlinova.navigation.kmmsample.browsernavigation

import com.eygraber.uri.Uri
import si.inova.kotlinova.navigation.kmmsample.WebScreenKey

class MultiBrowserUrlMatcher(vararg private val matchers: BrowserUrlMatcher) : BrowserUrlMatcher {

    override fun handleBrowserUrl(uri: Uri): List<WebScreenKey>? {
        return matchers.asSequence().mapNotNull { it.handleBrowserUrl(uri) }.firstOrNull()
    }
}
