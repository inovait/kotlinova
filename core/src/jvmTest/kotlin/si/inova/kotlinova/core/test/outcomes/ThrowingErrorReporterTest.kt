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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.exceptions.NoNetworkException

internal class ThrowingErrorReporterTest {
   private val scope = TestScope()
   private val reporter = ThrowingErrorReporter(scope)

   @Test
   internal fun `Ignore non-reportable exceptions`() = scope.runTest {
      reporter.report(NoNetworkException())
   }

   @Test
   internal fun `Fail test by default when something is reported`() {
      shouldThrow<AssertionError> {
         scope.runTest {
            reporter.report(DataParsingException())
         }
      }
   }

   @Test
   internal fun `Do not fail test when disabled`() = scope.runTest {
      reporter.reportToTestScope = false

      reporter.report(DataParsingException())
   }

   @Test
   internal fun `Collect thrown exceptions`() {
      shouldThrow<AssertionError> {
         scope.runTest {
            reporter.report(DataParsingException())
            reporter.report(DataParsingException())

            reporter.receivedExceptions.apply {
               shouldHaveSize(2)

               elementAt(0) should beInstanceOf<DataParsingException>()
               elementAt(1) should beInstanceOf<DataParsingException>()
            }
         }
      }
   }

   @Test
   internal fun `Collect thrown exceptions even when test scope reporting is disabled`() = scope.runTest {
      reporter.reportToTestScope = false

      reporter.report(DataParsingException())
      reporter.report(DataParsingException())

      reporter.receivedExceptions.apply {
         shouldHaveSize(2)

         elementAt(0) should beInstanceOf<DataParsingException>()
         elementAt(1) should beInstanceOf<DataParsingException>()
      }
   }
}
