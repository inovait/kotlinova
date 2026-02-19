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

package si.inova.kotlinova.core.flow

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome
import kotlin.time.Duration.Companion.milliseconds

/**
 * Returns a flow that will pass all data from the original flow, except that it will ensure that [Outcome.Progress] is never
 * emitted for a very short amount of time, that would cause blinking on the screen. It achieves that by:
 *
 * * Not emitting [Outcome.Progress] unless progress takes at least [waitThisLongToShowLoadingMs] milliseconds.
 *   If [doNotWaitForInterimLoadings] is set to true, this will only apply to the initial loading, not to the subsequent
 *   loadings that happen after a refresh.
 * * Once [Outcome.Progress] will be emitted, it will not emit any non-progress
 *   state for at least [keepLoadingActiveForAtLeastMs] milliseconds
 */
fun <T> Flow<Outcome<T>>.withBlinkingPrevention(
   waitThisLongToShowLoadingMs: Long = 100,
   keepLoadingActiveForAtLeastMs: Long = 500,
   doNotWaitForInterimLoadings: Boolean = false,
): Flow<Outcome<T>> {
   return BlinkingPrevention(
      this,
      waitThisLongToShowLoadingMs,
      keepLoadingActiveForAtLeastMs,
      doNotWaitForInterimLoadings
   )
}

private class BlinkingPrevention<T>(
   private val upstream: Flow<Outcome<T>>,
   private val waitThisLongToShowLoadingMs: Long = 100,
   private val keepLoadingActiveForAtLeastMs: Long = 500,
   private val doNotWaitForInterimLoadings: Boolean = false,
) : Flow<Outcome<T>> {
   var waitingToSeeIfLoadingJustFlashes = false
   var waitingForProlongedLoadingToFinish = false
   var lastData: T? = null
   var lastProgress: Outcome.Progress<T>? = null
   var lastError: CauseException? = null
   var gotSuccessDuringLoading = false
   var gotAtLeastOneSuccess = false

   @Suppress("CognitiveComplexMethod") // Splitting this would make it even worse
   override suspend fun collect(collector: FlowCollector<Outcome<T>>) {
      flow {
         coroutineScope {
            val upstreamChannel = upstream.produceIn(this)

            while (isActive) {
               select<Unit> {
                  upstreamChannel.onReceive {
                     when (it) {
                        is Outcome.Progress -> {
                           lastData = it.data
                           lastProgress = it

                           if (waitingForProlongedLoadingToFinish || (gotAtLeastOneSuccess && doNotWaitForInterimLoadings)) {
                              emitCurrentProgress()
                           } else if (waitingToSeeIfLoadingJustFlashes) {
                              // Do nothing, only update current data
                           } else {
                              waitingToSeeIfLoadingJustFlashes = true
                           }
                        }

                        is Outcome.Success -> {
                           gotAtLeastOneSuccess = true

                           if (waitingForProlongedLoadingToFinish) {
                              lastData = it.data
                              emitCurrentProgress()
                              gotSuccessDuringLoading = true
                           } else {
                              waitingToSeeIfLoadingJustFlashes = false
                              emit(it)
                           }
                        }

                        is Outcome.Error -> {
                           if (waitingForProlongedLoadingToFinish) {
                              lastData = it.data
                              lastError = it.exception
                              emitCurrentProgress()
                           } else {
                              waitingToSeeIfLoadingJustFlashes = false
                              lastError = null
                              emit(it)
                           }
                        }
                     }
                  }

                  if (waitingToSeeIfLoadingJustFlashes) {
                     showLoadingAfterTimeout(this, this@flow)
                  }

                  if (waitingForProlongedLoadingToFinish) {
                     continueAfterLoadingFinishes(this, this@flow)
                  }
               }
            }
         }
      }.collect(collector)
   }

   private fun continueAfterLoadingFinishes(
      selectBuilder: SelectBuilder<Unit>,
      flowCollector: FlowCollector<Outcome<T>>,
   ) {
      selectBuilder.onTimeout(keepLoadingActiveForAtLeastMs.milliseconds) {
         val currentLastError = lastError
         if (currentLastError != null) {
            flowCollector.emit(Outcome.Error(currentLastError, lastData))
            lastError = null
         } else if (gotSuccessDuringLoading) {
            flowCollector.emit(Outcome.Success(requireNotNull(lastData)))
            gotSuccessDuringLoading = false
         }
         waitingForProlongedLoadingToFinish = false
      }
   }

   private fun showLoadingAfterTimeout(
      selectBuilder: SelectBuilder<Unit>,
      flowCollector: FlowCollector<Outcome<T>>,
   ) {
      selectBuilder.onTimeout(waitThisLongToShowLoadingMs.milliseconds) {
         flowCollector.emitCurrentProgress()
         waitingToSeeIfLoadingJustFlashes = false
         waitingForProlongedLoadingToFinish = true
      }
   }

   private suspend fun FlowCollector<Outcome<T>>.emitCurrentProgress() {
      emit(lastProgress?.copy(data = lastData) ?: Outcome.Progress(lastData))
   }
}
