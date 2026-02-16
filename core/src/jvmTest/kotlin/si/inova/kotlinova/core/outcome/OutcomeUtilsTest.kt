/*
 * Copyright 2026 INOVA IT d.o.o.
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

package si.inova.kotlinova.core.outcome

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.exceptions.NoNetworkException

class OutcomeUtilsTest {
   @Test
   fun `firstSuccessOrThrow - emit result from a success`() = runTest {
      flowOf(Outcome.Success(1))
         .firstSuccessOrThrow() shouldBe 1
   }

   @Test
   fun `firstSuccessOrThrow - ignore subsequent successes`() = runTest {
      flowOf(
         Outcome.Success(1),
         Outcome.Success(2)
      )
         .firstSuccessOrThrow() shouldBe 1
   }

   @Test
   fun `firstSuccessOrThrow - ignore progresses`() = runTest {
      flowOf(
         Outcome.Progress(),
         Outcome.Progress(2),
         Outcome.Success(1),
         Outcome.Progress(3),
      )
         .firstSuccessOrThrow() shouldBe 1
   }

   @Test
   fun `firstSuccessOrThrow - throw failures`() = runTest {
      shouldThrow<NoNetworkException> {
         flowOf(
            Outcome.Progress(),
            Outcome.Progress(2),
            Outcome.Error(NoNetworkException()),
            Outcome.Success(1),
            Outcome.Progress(3),
         )
            .firstSuccessOrThrow()
      }
   }
}
