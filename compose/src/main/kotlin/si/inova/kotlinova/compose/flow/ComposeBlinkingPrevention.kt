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

package si.inova.kotlinova.compose.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.flow.withBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome

/**
 * Collect a flow in a way that it will ensure that [Outcome.Progress] is never
 * emitted for a very short amount of time, that would cause blinking on the screen. It achieves that by:
 *
 * * Not emitting [Outcome.Progress] unless progress takes at least [waitThisLongToShowLoadingMs] milliseconds.
 *   If [doNotWaitForInterimLoadings] is set to true, this will only apply to the initial loading, not to the subsequent
 *   loadings that happen after a refresh. Set this flag, when this flow is bound to Pull-to-refresh.
 * * Once [Outcome.Progress] will be emitted, it will not emit any non-progress
 *   state for at least [keepLoadingActiveForAtLeastMs] milliseconds
 *
 * Due to above behavior, initial state of this state will be *null*. In this case, do not display anything on your screen.
 *
 * @see collectAsStateWithLifecycle
 * @see withBlinkingPrevention
 */
@Composable
fun <T> Flow<Outcome<T>>.collectAsStateWithLifecycleAndBlinkingPrevention(
   waitThisLongToShowLoadingMs: Long = 100,
   keepLoadingActiveForAtLeastMs: Long = 500,
   doNotWaitForInterimLoadings: Boolean = false,
): State<Outcome<T>?> {
   val filteredFlow = remember(this) {
      this.withBlinkingPrevention(
         waitThisLongToShowLoadingMs,
         keepLoadingActiveForAtLeastMs,
         doNotWaitForInterimLoadings
      )
   }

   return filteredFlow.collectAsStateWithLifecycle(null)
}
