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

package si.inova.kotlinova.navigation.deeplink

import android.net.Uri
import androidx.collection.ArrayMap
import androidx.core.net.toUri
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Attempt to match deep link with one of the provided patterns.
 * (See [Developer docs](https://developer.android.com/guide/navigation/navigation-deep-link#implicit)
 * for how to create valid patterns - this method uses same matching engine as AndroidX Navigation underneath).
 *
 * If link is matched, [mapper] is called with map of matched arguments. Whatever you return from the mapper is forwarded
 * to the return of this method.
 */
inline fun Uri.matchDeepLink(
   vararg patterns: String,
   mapper: (Map<String, String>) -> NavigationInstruction?,
): NavigationInstruction? {
   val arguments = patterns.asSequence().mapNotNull {
      NavDeepLink(it).getMatchingArguments(this)
   }.firstOrNull() ?: return null

   return mapper(arguments)
}

/**
 * NavDeepLink encapsulates the parsing and matching of a navigation deep link.
 **
 * Copied and adapted from https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:navigation/navigation-common/src/main/java/androidx/navigation/NavDeepLink.kt
 */
@PublishedApi
internal class NavDeepLink(
   val uriPattern: String,
) {
   // path
   private val pathArgs = mutableListOf<String>()
   private var pathRegex: String? = null
   private val pathPattern by lazy {
      pathRegex?.let { Pattern.compile(it, Pattern.CASE_INSENSITIVE) }
   }

   // query
   private val isParameterizedQuery by lazy {
      uriPattern.toUri().query != null
   }
   private val queryArgsMap by lazy(LazyThreadSafetyMode.NONE) { parseQuery() }
   private var isSingleQueryParamValueOnly = false

   // fragment
   private val fragArgsAndRegex: Pair<MutableList<String>, String>? by lazy(LazyThreadSafetyMode.NONE) { parseFragment() }
   private val fragArgs by lazy(LazyThreadSafetyMode.NONE) {
      fragArgsAndRegex?.first ?: mutableListOf()
   }
   private val fragRegex by lazy(LazyThreadSafetyMode.NONE) {
      fragArgsAndRegex?.second
   }
   private val fragPattern by lazy {
      fragRegex?.let { Pattern.compile(it, Pattern.CASE_INSENSITIVE) }
   }

   /** Arguments present in the deep link, including both path and query arguments. */
   var isExactDeepLink: Boolean = false
      internal set

   private fun buildRegex(
      uri: String,
      args: MutableList<String>,
      uriRegex: StringBuilder,
   ) {
      val matcher = FILL_IN_PATTERN.matcher(uri)
      var appendPos = 0
      while (matcher.find()) {
         val argName = matcher.group(1) as String
         args.add(argName)
         // Use Pattern.quote() to treat the input string as a literal
         if (matcher.start() > appendPos) {
            uriRegex.append(Pattern.quote(uri.substring(appendPos, matcher.start())))
         }
         uriRegex.append("([^/]+?)")
         appendPos = matcher.end()
      }
      if (appendPos < uri.length) {
         // Use Pattern.quote() to treat the input string as a literal
         uriRegex.append(Pattern.quote(uri.substring(appendPos)))
      }
   }

   /**
    * Pattern.compile has no nullability for the regex parameter
    */
   @Suppress("NullableCollection")
   fun getMatchingArguments(
      deepLink: Uri,
   ): Map<String, String>? {
      // first check overall uri pattern for quick return if general pattern does not match
      val matcher = pathPattern?.matcher(deepLink.toString()) ?: return null
      if (!matcher.matches()) {
         return null
      }
      // get matching path and query arguments and store in bundle
      val map = ArrayMap<String, String>()
      if (!getMatchingPathArguments(matcher, map)) return null
      if (isParameterizedQuery && !getMatchingQueryArguments(deepLink, map)) {
         return null
      }
      // no match on optional fragment should not prevent a link from matching otherwise
      getMatchingUriFragment(deepLink.fragment, map)

      return map
   }

   private fun getMatchingUriFragment(
      fragment: String?,
      map: MutableMap<String, String>,
   ) {
      // Base condition of a matching fragment is a complete match on regex pattern. If a
      // required fragment arg is present while regex does not match, this will be caught later
      // on as a non-match when we check for presence of required args in the bundle.
      val matcher = fragPattern?.matcher(fragment.orEmpty()) ?: return
      if (!matcher.matches()) return

      this.fragArgs.mapIndexed { index, argumentName ->
         val value = Uri.decode(matcher.group(index + 1))
         map[argumentName] = value
      }
   }

   private fun getMatchingPathArguments(
      matcher: Matcher,
      map: MutableMap<String, String>,
   ): Boolean {
      this.pathArgs.mapIndexed { index, argumentName ->
         val value = Uri.decode(matcher.group(index + 1))
         map[argumentName] = value
      }
      return true
   }

   private fun getMatchingQueryArguments(
      deepLink: Uri,
      map: MutableMap<String, String>,
   ): Boolean {
      queryArgsMap.forEach { entry ->
         val paramName = entry.key
         val storedParam = entry.value

         val queryParamValue = deepLink.getQueryParameter(paramName) ?: return@forEach
         for (targetParamName in storedParam.arguments) {
            map[targetParamName] = queryParamValue
         }
      }
      return true
   }

   /**
    * Used to maintain query parameters and the mArguments they match with.
    */
   private class ParamQuery {
      var paramRegex: String? = null
      val arguments = mutableListOf<String>()

      fun addArgumentName(name: String) {
         arguments.add(name)
      }
   }

   private companion object {
      private val SCHEME_PATTERN = Pattern.compile("^[a-zA-Z]+[+\\w\\-.]*:")
      private val FILL_IN_PATTERN = Pattern.compile("\\{(.+?)\\}")
   }

   private fun parsePath() {
      val uriRegex = StringBuilder("^")
      // append scheme pattern
      if (!SCHEME_PATTERN.matcher(uriPattern).find()) {
         uriRegex.append("http[s]?://")
      }
      // extract beginning of uriPattern until it hits either a query(?), a framgment(#), or
      // end of uriPattern
      val matcher = Pattern.compile("(\\?|#|$)").matcher(uriPattern)
      matcher.find()
      buildRegex(uriPattern.substring(0, matcher.start()), pathArgs, uriRegex)
      isExactDeepLink = !uriRegex.contains(".*") && !uriRegex.contains("([^/]+?)")
      // Match either the end of string if all params are optional or match the
      // question mark (or pound symbol) and 0 or more characters after it
      uriRegex.append("($|(\\?(.)*)|(\\#(.)*))")

      // we need to specifically escape any .* instances to ensure
      // they are still treated as wildcards in our final regex
      pathRegex = uriRegex.toString().replace(".*", "\\E.*\\Q")
   }

   private fun parseQuery(): MutableMap<String, ParamQuery> {
      val paramArgMap = mutableMapOf<String, ParamQuery>()
      if (!isParameterizedQuery) return paramArgMap
      val uri = uriPattern.toUri()

      for (paramName in uri.queryParameterNames) {
         val argRegex = StringBuilder()
         val queryParams = uri.getQueryParameters(paramName)
         require(queryParams.size <= 1) {
            "Query parameter $paramName must only be present once in $uriPattern. " +
               "To support repeated query parameters, use an array type for your " +
               "argument and the pattern provided in your URI will be used to " +
               "parse each query parameter instance."
         }
         val queryParam = queryParams.firstOrNull()
            ?: paramName.apply { isSingleQueryParamValueOnly = true }
         val matcher = FILL_IN_PATTERN.matcher(queryParam)
         var appendPos = 0
         val param = ParamQuery()
         // Build the regex for each query param
         while (matcher.find()) {
            // matcher.group(1) as String = "tab" (the extracted param arg from {tab})
            param.addArgumentName(matcher.group(1) as String)
            argRegex.append(
               Pattern.quote(
                  queryParam.substring(
                     appendPos,
                     matcher.start()
                  )
               )
            )
            argRegex.append("(.+?)?")
            appendPos = matcher.end()
         }
         if (appendPos < queryParam.length) {
            argRegex.append(Pattern.quote(queryParam.substring(appendPos)))
         }

         // Save the regex with wildcards unquoted, and add the param to the map with its
         // name as the key
         param.paramRegex = argRegex.toString().replace(".*", "\\E.*\\Q")
         paramArgMap[paramName] = param
      }
      return paramArgMap
   }

   private fun parseFragment(): Pair<MutableList<String>, String>? {
      if (uriPattern.toUri().fragment == null) return null

      val fragArgs = mutableListOf<String>()
      val fragment = uriPattern.toUri().fragment
      val fragRegex = StringBuilder()
      buildRegex(requireNotNull(fragment), fragArgs, fragRegex)
      return fragArgs to fragRegex.toString()
   }

   init {
      parsePath()
   }
}
