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
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.reporting.ErrorReporter

/**
 * Return [CoroutineResourceManager] that runs with test dispatcher and
 * reports all unknown exceptions to the test coroutine runner.
 */
fun TestScope.testCoroutineResourceManager(): CoroutineResourceManager {
   return CoroutineResourceManager(backgroundScope, ThrowingErrorReporter(this))
}

/**
ErrorReporter that will collect all reported exceptions and optionally throw them at the end of the test

All exceptions with [CauseException.shouldReport] set to false will be ignored.

[receivedExceptions] will contain all exceptions that were reported. If [reportToTestScope] is set to true (default),
all exceptions will also be reported to the provided [TestScope], causing test to fail at the end of the run.
 */
class ThrowingErrorReporter(private val testScope: TestScope) : ErrorReporter {
   val receivedExceptions = ArrayList<Throwable>()
   var reportToTestScope: Boolean = true

   private val coroutineExceptionHandler = requireNotNull(
      testScope.coroutineContext[CoroutineExceptionHandler]
   ) { "TestScope must have CoroutineExceptionHandler registered" }

   override fun report(throwable: Throwable) {
      if (throwable !is CauseException || throwable.shouldReport) {
         receivedExceptions += throwable

         if (reportToTestScope) {
            coroutineExceptionHandler.handleException(
               testScope.coroutineContext,
               AssertionError("Got reported exception while executing this test", throwable)
            )
         }
      }
   }
}
