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

package si.inova.kotlinova.core.outcome

import androidx.compose.runtime.Immutable
import si.inova.kotlinova.core.outcome.Outcome.Error
import si.inova.kotlinova.core.outcome.Outcome.Progress
import si.inova.kotlinova.core.outcome.Outcome.Success

/**
 * Standard wrapper for an operation. It can be either [Progress], [Success] or [Error].
 */
@Immutable
sealed class Outcome<out T> {
   abstract val data: T?

   @Immutable
   data class Progress<out T>(
      override val data: T? = null,
      val progress: Float? = null,
      val style: LoadingStyle = LoadingStyle.NORMAL
   ) : Outcome<T>()

   @Immutable
   data class Success<out T>(override val data: T) : Outcome<T>()

   @Immutable
   data class Error<out T>(val exception: CauseException, override val data: T? = null) : Outcome<T>()
}

enum class LoadingStyle {
   NORMAL,
   ADDITIONAL_DATA,
   USER_REQUESTED_REFRESH
}
