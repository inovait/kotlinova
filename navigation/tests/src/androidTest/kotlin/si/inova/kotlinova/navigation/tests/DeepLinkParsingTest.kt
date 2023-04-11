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

package si.inova.kotlinova.navigation.tests

import android.net.Uri
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test
import si.inova.kotlinova.navigation.deeplink.matchDeepLink

class DeepLinkParsingTest {
   @Test
   internal fun testDeepLinkParsing() {
      getDeepLinkMatchingResult("test://abc", "test://abc") shouldBe emptyMap()

      getDeepLinkMatchingResult("test://abc", "test://def").shouldBeNull()

      getDeepLinkMatchingResult("test://abc", "test://{argument}") shouldBe
         mapOf("argument" to "abc")
      getDeepLinkMatchingResult("test://abc?a=b", "test://{argument}?a={qp}") shouldBe
         mapOf("argument" to "abc", "qp" to "b")
      getDeepLinkMatchingResult("test://abc", "test://{argument}?a={qp}") shouldBe
         mapOf("argument" to "abc")
   }

   private fun getDeepLinkMatchingResult(uri: String, pattern: String): Map<String, String>? {
      var result: Map<String, String>? = null

      Uri.parse(uri).matchDeepLink(pattern) {
         result = it
         null
      }

      return result
   }
}
