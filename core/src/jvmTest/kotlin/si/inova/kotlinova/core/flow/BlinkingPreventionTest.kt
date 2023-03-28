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

package si.inova.kotlinova.core.flow

import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.test.outcomes.shouldBeErrorWith
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWithData
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.util.testWithExceptions

internal class BlinkingPreventionTest {
   @Test
   internal fun `Do not switch to Loading for several milliseconds`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Success(1))
      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         awaitItem().shouldNotBeNull() shouldBeSuccessWithData 1

         source.value = Outcome.Progress(1)
         runCurrent()
         expectNoEvents()

         advanceTimeBy(150)
         runCurrent()
         awaitItem().shouldNotBeNull() shouldBeProgressWithData 1
      }
   }

   @Test
   internal fun `Switch to interim Loading immediately when doNotWaitForInterimLoadings is set`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Success(1))
      val flowWithoutBlinking = source.withBlinkingPrevention(
         doNotWaitForInterimLoadings = true
      )

      flowWithoutBlinking.testWithExceptions {
         awaitItem().shouldNotBeNull() shouldBeSuccessWithData 1

         source.value = Outcome.Progress(1)
         runCurrent()
         expectMostRecentItem().shouldNotBeNull() shouldBeProgressWithData 1
      }
   }

   @Test
   internal fun `Start with empty flow if flow starts with Loading for several seconds`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Progress(1))

      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         expectNoEvents()

         advanceTimeBy(150)
         runCurrent()
         awaitItem().shouldNotBeNull() shouldBeProgressWithData 1

         advanceTimeBy(9_000)
         expectNoEvents()
      }
   }

   @Test
   internal fun `Start with empty flow if flow starts with Loading for several seconds even if there is no data`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Progress())

      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         expectNoEvents()

         advanceTimeBy(150)
         runCurrent()
         awaitItem().shouldNotBeNull() shouldBeProgressWithData null

         advanceTimeBy(9_000)
         expectNoEvents()
      }
   }

   @Test
   internal fun `Start with empty flow if flow starts with Loading for several seconds even if interim wait is enabled`() =
      runTest {
         val source = MutableStateFlow<Outcome<Int>>(Outcome.Progress(1))

         val flowWithoutBlinking = source.withBlinkingPrevention(doNotWaitForInterimLoadings = true)

         flowWithoutBlinking.testWithExceptions {
            expectNoEvents()

            advanceTimeBy(150)
            runCurrent()
            awaitItem().shouldNotBeNull() shouldBeProgressWithData 1
         }
      }

   @Test
   internal fun `Switch directly to Success if initial Loading is very short`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Progress())

      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         expectNoEvents()

         advanceTimeBy(50)
         runCurrent()
         expectNoEvents()

         source.value = Outcome.Success(1)
         runCurrent()
         awaitItem().shouldNotBeNull() shouldBeSuccessWithData 1
      }
   }

   @Test
   internal fun `Switch directly to Error if initial Loading is very short`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Progress())

      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         expectNoEvents()

         advanceTimeBy(50)
         runCurrent()
         expectNoEvents()

         source.value = Outcome.Error(NoNetworkException(), data = 3)
         runCurrent()
         awaitItem().shouldNotBeNull().shouldBeErrorWith(expectedData = 3, exceptionType = NoNetworkException::class.java)
      }
   }

   @Test
   internal fun `Switch directly to next Success if interim Loading is very short`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Success(1))

      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         awaitItem().shouldNotBeNull() shouldBeSuccessWithData 1

         source.value = Outcome.Progress(2)
         advanceTimeBy(50)
         runCurrent()
         expectNoEvents()

         source.value = Outcome.Success(3)
         runCurrent()
         awaitItem().shouldNotBeNull() shouldBeSuccessWithData 3
      }
   }

   @Test
   internal fun `Keep loading active for a minimum amount of time before switching to Success`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Success(1))

      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         awaitItem() // Ignore initial Success

         source.value = Outcome.Progress(1)
         advanceTimeBy(150)
         runCurrent()
         awaitItem()

         advanceTimeBy(150)
         source.value = Outcome.Success(2)
         runCurrent()
         awaitItem().shouldNotBeNull() shouldBeProgressWithData 2

         advanceTimeBy(400)
         runCurrent()
         awaitItem().shouldNotBeNull() shouldBeSuccessWithData 2
      }
   }

   @Test
   internal fun `Keep loading active for a minimum amount of time before switching to Failure`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Success(1))

      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         awaitItem() // Ignore initial Success

         source.value = Outcome.Progress(1)
         advanceTimeBy(150)
         runCurrent()
         awaitItem()

         advanceTimeBy(150)
         source.value = Outcome.Error(NoNetworkException(), 2)
         runCurrent()
         awaitItem().shouldNotBeNull() shouldBeProgressWithData 2

         advanceTimeBy(400)
         runCurrent()
         awaitItem().shouldNotBeNull().shouldBeErrorWith(expectedData = 2, exceptionType = NoNetworkException::class.java)
      }
   }

   @Test
   internal fun `Switch to Success immediately if loading has been happening for some time`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Success(1))

      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         awaitItem() // Ignore initial Success

         source.value = Outcome.Progress(1)
         advanceTimeBy(150)
         runCurrent()
         awaitItem()

         advanceTimeBy(1000)
         source.value = Outcome.Success(2)
         runCurrent()
         awaitItem().shouldNotBeNull() shouldBeSuccessWithData 2
      }
   }

   @Test
   internal fun `Switch to Error immediately if loading has been happening for some time`() = runTest {
      val source = MutableStateFlow<Outcome<Int>>(Outcome.Success(1))

      val flowWithoutBlinking = source.withBlinkingPrevention()

      flowWithoutBlinking.testWithExceptions {
         awaitItem() // Ignore initial Success

         source.value = Outcome.Progress(1)
         advanceTimeBy(150)
         runCurrent()
         awaitItem()

         advanceTimeBy(1000)
         source.value = Outcome.Error(NoNetworkException())
         runCurrent()
         awaitItem().shouldNotBeNull().shouldBeErrorWith(exceptionType = NoNetworkException::class.java)
      }
   }
}
