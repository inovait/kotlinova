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

package si.inova.kotlinova.retrofit

import kotlinx.coroutines.CompletableDeferred

/**
 * Helper for creating fake retrofit services. It allows you to easily replace any call with either infinite loading
 * or error.
 *
 * To use this, you need to call [ServiceTestingHelper.intercept] in your fake
 * retrofit service's methods before you return the fake value.
 */
class ServiceTestingHelper : FakeService {
   private var nextInterception: InterceptionStyle? = null
   private var defaultInterception: InterceptionStyle = InterceptionStyle.None

   private var infiniteLoadCompletable: CompletableDeferred<Unit>? = null
   private var immediatelyComplete = false

   override var numServiceCalls: Int = 0

   override fun interceptNextCallWith(style: InterceptionStyle) {
      nextInterception = style
   }

   override fun interceptAllFutureCallsWith(style: InterceptionStyle) {
      defaultInterception = style
   }

   override fun completeInfiniteLoad() {
      val infiniteLoadCompletable = infiniteLoadCompletable
      if (infiniteLoadCompletable == null) {
         immediatelyComplete = true
      } else {
         infiniteLoadCompletable.complete(Unit)
         this.infiniteLoadCompletable = null

         immediatelyComplete = false
      }
   }

   suspend fun intercept() {
      val nextInterception = nextInterception ?: defaultInterception
      this.nextInterception = null

      numServiceCalls++

      return when (nextInterception) {
         is InterceptionStyle.None -> Unit
         is InterceptionStyle.InfiniteLoad -> {
            if (immediatelyComplete) {
               immediatelyComplete = false
               return
            }

            val completable = CompletableDeferred<Unit>()
            this.infiniteLoadCompletable = completable

            completable.await()
         }

         is InterceptionStyle.Error -> throw nextInterception.exception
      }
   }

   fun reset() {
      completeInfiniteLoad()
      nextInterception = InterceptionStyle.None
      immediatelyComplete = false
      numServiceCalls = 0
   }
}

sealed class InterceptionStyle {
   /**
    * Return value normally
    */
   object None : InterceptionStyle()

   /**
    * Suspend until [FakeService.completeInfiniteLoad] is called
    */
   object InfiniteLoad : InterceptionStyle()

   /**
    * Throw exception
    */
   data class Error(val exception: Throwable) : InterceptionStyle()
}

interface FakeService {
   /**
    * Number of load calls that have been made to this service
    */
   val numServiceCalls: Int

   /**
    * When set, next call to this service will behave in accordance with provided [InterceptionStyle].
    */
   fun interceptNextCallWith(style: InterceptionStyle)

   /**
    * When set, all future calls to this service will behave in accordance with provided [InterceptionStyle].
    */
   fun interceptAllFutureCallsWith(style: InterceptionStyle)

   /**
    * Complete any previous calls with [InterceptionStyle.InfiniteLoad] set
    */
   fun completeInfiniteLoad()
}
