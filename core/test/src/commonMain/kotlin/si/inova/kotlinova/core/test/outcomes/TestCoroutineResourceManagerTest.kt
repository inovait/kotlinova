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

package si.inova.kotlinova.core.test.outcomes

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.test.TestScope
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.reporting.ErrorReporter

/**
 * Return [CoroutineResourceManager] that runs with test dispatcher and
 * reports all unknown exceptions to the test coroutine runner.
 */
fun TestScope.testCoroutineResourceManager(): CoroutineResourceManager {
   return CoroutineResourceManager(backgroundScope, throwingErrorReporter())
}

/**
 * ErrorReporter that will throw all reported exceptions at the end of the test
 */
fun TestScope.throwingErrorReporter(): ErrorReporter {
   val coroutineExceptionHandler = requireNotNull(coroutineContext[CoroutineExceptionHandler])
   return ErrorReporter { coroutineExceptionHandler.handleException(coroutineContext, it) }
}
