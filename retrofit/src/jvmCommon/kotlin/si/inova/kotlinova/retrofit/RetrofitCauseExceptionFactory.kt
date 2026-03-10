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

package si.inova.kotlinova.retrofit

import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import java.io.IOException

/**
 * An interface to transfrom a raw retrofit exception into semantic CauseException.
 */
typealias RetrofitCauseExceptionFactory = (original: Throwable, requestDetails: String) -> CauseException

object DefaultRetrofitCauseExceptionFactory : RetrofitCauseExceptionFactory {
   override fun invoke(original: Throwable, requestDetails: String): CauseException {
      return when (original) {
         is CauseException -> original
         is IOException -> NoNetworkException(cause = original, message = "Failed to load $requestDetails")
         else -> UnknownCauseException("Failed to load $requestDetails", cause = original)
      }
   }
}
