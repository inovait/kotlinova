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

import androidx.compose.ui.test.junit4.ComposeTestRule
import dispatch.android.espresso.IdlingDispatcher
import dispatch.core.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Register all passed dispatchers as an idling resource for this test.
 */
fun ComposeTestRule.registerIdlingDispatchers(vararg dispatchers: CoroutineDispatcher) {
   for (dispatcher in dispatchers) {
      val espressoIdlingResource = when (dispatcher) {
         is IdlingDispatcher -> {
            dispatcher.counter
         }

         is FixedIdlingDispatcher -> {
            dispatcher.counter
         }

         else -> {
            throw IllegalArgumentException("Passed dispatcher '$dispatcher' is not an IdlingDispatcher")
         }
      }

      registerIdlingResource(espressoIdlingResource.asComposeIdlingResource())
   }
}

/**
 * Register all dispatchers provided by this dispatcher provider as an idling resource for this test.
 */
fun ComposeTestRule.registerIdlingDispatchers(dispatcherProvider: DispatcherProvider) {
   with(dispatcherProvider) {
      registerIdlingDispatchers(
         default,
         io,
         main,
         mainImmediate,
         unconfined
      )
   }
}
