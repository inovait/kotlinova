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

package si.inova.kotlinova.core.test

import dispatch.core.DispatcherProvider
import dispatch.core.MainImmediateCoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A [DispatcherProvider] that provides test dispatcher for all dispatchers.
 */
@OptIn(ExperimentalStdlibApi::class)
val TestScope.testDispatcherProvider: DispatcherProvider
   get() = SingleDispatcherProvider(requireNotNull(coroutineContext[CoroutineDispatcher]))

/**
 * [MainImmediateCoroutineScope] based on a [TestScope.backgroundScope].
 *
 * This scope will have a test dispatcher and will be automatically cancelled when test finishes.
 */
val TestScope.testMainImmediateBackgroundScope: MainImmediateCoroutineScope
   get() = MainImmediateCoroutineScope(SupervisorJob(backgroundScope.coroutineContext.job), testDispatcherProvider)

/**
 * Creates a [TestScope] with a [DispatcherProvider] installed that provides TestScope's test dispatchers as all dispatchers.
 */
fun TestScopeWithDispatcherProvider(context: CoroutineContext = EmptyCoroutineContext): TestScope {
   val testDispatcher = StandardTestDispatcher()
   val testDispatcherProvider = SingleDispatcherProvider(testDispatcher)
   return TestScope(context + testDispatcherProvider + testDispatcher)
}

/**
 * Dispatcher provider that provides a single dispatcher as every other
 */
class SingleDispatcherProvider(private val dispatcher: CoroutineDispatcher) : DispatcherProvider {
   override val default: CoroutineDispatcher
      get() = dispatcher
   override val io: CoroutineDispatcher
      get() = dispatcher
   override val main: CoroutineDispatcher
      get() = dispatcher
   override val mainImmediate: CoroutineDispatcher
      get() = dispatcher
   override val unconfined: CoroutineDispatcher
      get() = dispatcher
}
