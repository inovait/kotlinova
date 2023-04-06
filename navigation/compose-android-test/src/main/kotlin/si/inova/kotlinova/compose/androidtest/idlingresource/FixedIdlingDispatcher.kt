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

package si.inova.kotlinova.compose.androidtest.idlingresource

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

/**
 * [IdlingResource] helper for coroutines.  This class simply wraps a delegate [CoroutineDispatcher]
 * and keeps a running count of all coroutines it creates, decrementing the count when they complete.
 *
 * A copy of the [dispatch.android.espresso.IdlingDispatcher] with https://github.com/RBusarow/Dispatch/pull/619 fix applied.
 *
 *
 * @param delegate The [CoroutineDispatcher] which will be used for actual dispatch.
 * @see IdlingResource
 * @see CoroutineDispatcher
 */
class FixedIdlingDispatcher(
   val delegate: CoroutineDispatcher
) : CoroutineDispatcher() {

   /**
    * The [CountingIdlingResource] which is responsible for Espresso functionality.
    */
   val counter: CountingIdlingResource = CountingIdlingResource("IdlingResource for $this")

   /**
    * @return
    * * true if the [counter]'s count is zero
    * * false if the [counter]'s count is non-zero
    */
   fun isIdle(): Boolean = counter.isIdleNow

   /**
    * Counting implementation of the [dispatch][CoroutineDispatcher.dispatch] function.
    *
    * The count is incremented for every dispatch, and decremented for every completion, including suspension.
    */
   override fun dispatch(context: CoroutineContext, block: Runnable) {
      val runnable = Runnable {
         counter.increment()
         try {
            block.run()
         } finally {
            counter.decrement()
         }
      }
      delegate.dispatch(context, runnable)
   }

   /**
    * @suppress
    */
   override fun toString(): String = "CountingDispatcher delegating to $delegate"
}
