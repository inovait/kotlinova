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

package si.inova.kotlinova.core.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.time.TimeProvider
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Class that will only execute passed task if a particular time span has been passed without
 * another task being added.
 *
 * When [triggerFirstImmediately] flag is set, task will be triggered immediately if there was
 * no other task within [debouncingTimeMs]. Otherwise, it will wait for another [debouncingTimeMs]
 * to ensure no further tasks are there.
 */
class Debouncer(
   private val scope: CoroutineScope,
   private val timeProvider: TimeProvider,
   private val debouncingTimeMs: Long = 500L,
   private val triggerFirstImmediately: Boolean = false,
   private val targetContext: CoroutineContext = EmptyCoroutineContext,
) {
   private var previousJob: Job? = null
   private var lastStart = 0L

   fun executeDebouncing(task: suspend () -> Unit) {
      previousJob?.cancel()

      previousJob = scope.launch(targetContext) {
         if ((lastStart != 0L || !triggerFirstImmediately) &&
            (timeProvider.currentTimeMillis() - lastStart) < debouncingTimeMs
         ) {
            delay(debouncingTimeMs)
         }

         lastStart = System.currentTimeMillis()
         task()
      }
   }
}
