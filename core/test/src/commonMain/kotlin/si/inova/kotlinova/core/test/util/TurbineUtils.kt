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

package si.inova.kotlinova.core.test.util

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

/**
 * Start turbine test without eating exceptions.
 *
 * This is a workaround for the https://github.com/cashapp/turbine/issues/205,
 * will be deprecated when that issue fix gets released.
 *
 * @see Flow.test
 */
suspend fun <T> Flow<T>.testWithExceptions(
   timeout: Duration? = null,
   name: String? = null,
   validate: suspend ReceiveTurbine<T>.() -> Unit,
) {
   val coroutineExceptionHandler =
      requireNotNull(coroutineContext[CoroutineExceptionHandler]) {
         "testWithException should only be called inside runTest scope"
      }

   return this.catch {
      coroutineExceptionHandler.handleException(coroutineContext, it)
   }.test(timeout, name, validate)
}
