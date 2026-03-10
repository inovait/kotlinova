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

package si.inova.kotlinova.retrofit.moshi

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.outcome.CauseException

class MoshiRetrofitCauseExceptionFactory(
   private val defaultFactory: (original: Throwable, requestDetails: String) -> CauseException,
) : (Throwable, String) -> CauseException {
   override fun invoke(original: Throwable, requestDetails: String): CauseException {
      return when (original) {
         is JsonDataException, is JsonEncodingException -> DataParsingException(
            "Failed to parse $requestDetails",
            cause = original
         )

         else -> defaultFactory(original, requestDetails)
      }
   }
}
