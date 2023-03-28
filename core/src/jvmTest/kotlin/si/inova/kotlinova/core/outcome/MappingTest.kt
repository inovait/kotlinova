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

package si.inova.kotlinova.core.outcome

import io.kotest.assertions.assertSoftly
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.test.outcomes.shouldBeErrorWith
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWith
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWithData
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData

internal class MappingTest {
   @Test
   internal fun `Regular Map`() {
      assertSoftly {
         Outcome.Success(30).mapData { it * 2 } shouldBeSuccessWithData 60
         Outcome.Success(30).mapData { it.toString() } shouldBeSuccessWithData "30"
         Outcome.Progress(30).mapData { it * 2 } shouldBeProgressWithData 60
         Outcome.Progress(30).mapData { it.toString() } shouldBeProgressWithData "30"
         Outcome.Error(NoNetworkException(), 30).mapData { it * 2 }.shouldBeErrorWith(expectedData = 60)
         Outcome.Error(NoNetworkException(), 30).mapData { it.toString() }.shouldBeErrorWith(expectedData = "30")
      }
   }

   @Test
   internal fun `Switch map success into success`() = runTest {
      val flow = flowOf(Outcome.Success(10)).flatMapLatestOutcome { flowOf(Outcome.Success(it * 20)) }

      flow.first() shouldBeSuccessWithData 200
   }

   @Test
   internal fun `Switch map success into progress`() = runTest {
      val flow = flowOf(Outcome.Success(10))
         .flatMapLatestOutcome { flowOf(Outcome.Progress(it * 20, progress = 0.7f)) }

      flow.first().shouldBeProgressWith(expectedData = 200, expectedProgress = 0.7f)
   }

   @Test
   internal fun `Switch map success into failure`() = runTest {
      val flow = flowOf(Outcome.Success(10))
         .flatMapLatestOutcome { flowOf(Outcome.Error(NoNetworkException(), it * 20)) }

      flow.first().shouldBeErrorWith(expectedData = 200, exceptionType = NoNetworkException::class.java)
   }

   @Test
   internal fun `Switch map progress into success`() = runTest {
      val flow = flowOf(Outcome.Progress(10, 0.5f))
         .flatMapLatestOutcome { flowOf(Outcome.Success(it * 20)) }

      flow.first().shouldBeProgressWith(expectedData = 200, expectedProgress = 0.5f)
   }

   @Test
   internal fun `Switch map progress into progress`() = runTest {
      val flow = flowOf(Outcome.Progress(10, 0.5f))
         .flatMapLatestOutcome { flowOf(Outcome.Progress(it * 20, 0.7f)) }

      flow.first().shouldBeProgressWith(expectedData = 200, expectedProgress = 0.35f)
   }

   @Test
   internal fun `Switch map progress into failure`() = runTest {
      val flow = flowOf(Outcome.Progress(10, 0.5f))
         .flatMapLatestOutcome { flowOf(Outcome.Error(NoNetworkException(), it * 20)) }

      flow.first().shouldBeErrorWith(expectedData = 200, exceptionType = NoNetworkException::class.java)
   }

   @Test
   internal fun `Switch map failure into success`() = runTest {
      val flow = flowOf(Outcome.Error(NoNetworkException(), 10f))
         .flatMapLatestOutcome { flowOf(Outcome.Success(it * 20)) }

      flow.first().shouldBeErrorWith(expectedData = 200, exceptionType = NoNetworkException::class.java)
   }

   @Test
   internal fun `Switch map failure into progress`() = runTest {
      val flow = flowOf(Outcome.Error(NoNetworkException(), 10f))
         .flatMapLatestOutcome { flowOf(Outcome.Progress(it * 20, 0.7f)) }

      flow.first().shouldBeErrorWith(expectedData = 200, exceptionType = NoNetworkException::class.java)
   }

   @Test
   internal fun `Switch map failure into failure`() = runTest {
      val flow = flowOf(Outcome.Error(NoNetworkException(), 10f))
         .flatMapLatestOutcome { flowOf(Outcome.Error(NoNetworkException(), it * 20)) }

      flow.first().shouldBeErrorWith(expectedData = 200, exceptionType = NoNetworkException::class.java)
   }

   @Test
   internal fun `Ignore switch map from progress when data is null`() = runTest {
      val flow = flowOf(Outcome.Progress<Int>(progress = 0.5f))
         .flatMapLatestOutcome { flowOf(Outcome.Error(NoNetworkException(), it * 20)) }

      flow.first().shouldBeProgressWith(expectedData = null, expectedProgress = 0.5f)
   }

   @Test
   internal fun `Ignore switch map from failure when data is null`() = runTest {
      val flow = flowOf(Outcome.Error<Int>(NoNetworkException()))
         .flatMapLatestOutcome { flowOf(Outcome.Error(NoNetworkException(), it * 20)) }

      flow.first().shouldBeErrorWith(expectedData = null, exceptionType = NoNetworkException::class.java)
   }
}
