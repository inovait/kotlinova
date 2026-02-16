/*
 * Copyright 2024 INOVA IT d.o.o.
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Execute passed [block], returning its value, or [Outcome.Error] if block throws any exception
 */
inline fun <T> catchIntoOutcome(block: () -> Outcome<T>): Outcome<T> {
   return try {
      block()
   } catch (e: CancellationException) {
      throw e
   } catch (e: CauseException) {
      Outcome.Error(e)
   } catch (e: Exception) {
      Outcome.Error(UnknownCauseException(cause = e))
   }
}

/**
 * Returns a result of the [Outcome.Success] emitted by this flow or
 * throws an exception from the [Outcome.Error] emitted by this flow,
 * whichever is emitted first.
 */
suspend fun <T> Flow<Outcome<T>>.firstSuccessOrThrow(): T {
   val result = first {
      it is Outcome.Success || it is Outcome.Error
   }

   return when (result) {
      is Outcome.Success -> result.data
      is Outcome.Error -> throw result.exception
      is Outcome.Progress -> error("Result should never be progress")
   }
}
