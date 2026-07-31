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

package si.inova.kotlinova.navigation.kmmsample.browsernavigation

import com.eygraber.uri.Uri
import si.inova.kotlinova.navigation.kmmsample.WebScreenKey

/**
 * Attempt to match deep link with one of the provided patterns.
 * (See [Developer docs](https://developer.android.com/guide/navigation/navigation-deep-link#implicit)
 * for how to create valid patterns - this method uses same matching engine as AndroidX Navigation underneath).
 *
 * If link is matched, [mapper] is called with map of matched arguments. If you return non-null instructions from this
 * mapper, user will be navigated to those instructions.
 */
class PatternMatcher(
    pattern: String,
    private val mapper: (args: Map<String, String>) -> List<WebScreenKey>?
) : BrowserUrlMatcher {
    private val matchingPathSegments: List<PathSegment>
    private val requiredQueryMatches: Map<String, String>

    init {
        val parsedPattern = Uri.parse(pattern)
        matchingPathSegments = parsedPattern.pathSegments.map { pathSegment ->
            val placeholderMatch = PLACEHOLDER_REGEX.matchEntire(pathSegment)?.groupValues?.elementAt(1)

            if (placeholderMatch != null) {
                PathSegment.SaveValue(placeholderMatch)
            } else {
                PathSegment.Match(pathSegment)
            }
        }

        requiredQueryMatches = parsedPattern.getQueryParameterNames().mapNotNull { queryParameterName ->
            val queryParameterValue = parsedPattern.getQueryParameter(queryParameterName) ?: return@mapNotNull null
            val placeholderKey = PLACEHOLDER_REGEX.matchEntire(queryParameterValue)
                ?.groupValues
                ?.elementAt(1)
                ?: return@mapNotNull null

            queryParameterName to placeholderKey
        }.toMap()
    }

    override fun handleBrowserUrl(uri: Uri): List<WebScreenKey>? {
        val parameterMap = HashMap<String, String>()
        if (uri.pathSegments.size != matchingPathSegments.size) {
            return null
        }

        uri.pathSegments.zip(matchingPathSegments) { pathSegment, matchInstructions ->
            when (matchInstructions) {
                is PathSegment.Match -> {
                    if (pathSegment != matchInstructions.value) {
                        return null
                    }
                }

                is PathSegment.SaveValue -> {
                    parameterMap[matchInstructions.key] = pathSegment
                }
            }
        }

        for ((queryParameterName, parameterKey) in requiredQueryMatches) {
            parameterMap[parameterKey] = uri.getQueryParameter(queryParameterName) ?: return null
        }


        return mapper(parameterMap)
    }

    private sealed class PathSegment {
        data class SaveValue(val key: String) : PathSegment()
        data class Match(val value: String) : PathSegment()
    }
}

private val PLACEHOLDER_REGEX = Regex("""\{([^}]+)}""")
