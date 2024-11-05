/*
 * Copyright 2024 INOVA IT d.o.o.
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

import app.cash.turbine.test
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.test.outcomes.shouldBeErrorWith
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWithData

internal class CoroutineResourceManagerTest {
   private val reportedErrors = ArrayList<Throwable>()
   private val scope = TestScope()
   private val manager = CoroutineResourceManager(scope.backgroundScope) { reportedErrors += it }

   @Test
   internal fun `Launch job`() = scope.runTest {
      val resource = Any()

      var launched = false
      manager.launchBoundControlTask(resource) {
         launched = true
      }

      runCurrent()

      launched shouldBe true
   }

   @Test
   internal fun `isResourceTaken should report status of the resource`() = scope.runTest {
      val resourceA = Any()
      val resourceB = Any()

      val latch = CompletableDeferred<Unit>()

      manager.launchBoundControlTask(resourceA) {
         latch.await()
      }

      runCurrent()

      manager.isResourceTaken(resourceA) shouldBe true
      manager.isResourceTaken(resourceB) shouldBe false

      latch.complete(Unit)
      runCurrent()

      manager.isResourceTaken(resourceA) shouldBe false
   }

   @Test
   internal fun `cancelResource should cancel all jobs with this resource`() = scope.runTest {
      val resourceA = Any()
      val resourceB = Any()

      var cancelledA = false
      var cancelledB = false

      manager.launchBoundControlTask(resourceA) {
         try {
            awaitCancellation()
         } finally {
            cancelledA = true
         }
      }

      manager.launchBoundControlTask(resourceB) {
         try {
            awaitCancellation()
         } finally {
            cancelledB = true
         }
      }

      runCurrent()

      manager.cancelResource(resourceA)
      runCurrent()

      cancelledA shouldBe true
      cancelledB shouldBe false
   }

   @Test
   internal fun `Cancel previous jobs from that resources`() = scope.runTest {
      val resourceA = Any()
      val resourceB = Any()

      var runningA1 = false
      var runningA2 = false
      var runningB = false

      manager.launchBoundControlTask(resourceA) {
         try {
            runningA1 = true
            awaitCancellation()
         } finally {
            runningA1 = false
         }
      }

      manager.launchBoundControlTask(resourceB) {
         try {
            runningB = true
            awaitCancellation()
         } finally {
            runningB = false
         }
      }

      runCurrent()

      manager.launchBoundControlTask(resourceA) {
         try {
            runningA2 = true
            awaitCancellation()
         } finally {
            runningA2 = false
         }
      }

      runCurrent()

      runningA1 shouldBe false
      runningA2 shouldBe true
      runningB shouldBe true
   }

   @Test
   internal fun `Wait for previous job to stop`() = scope.runTest {
      val resource = Any()

      val latch = CompletableDeferred<Unit>()

      var running1 = false
      var running2 = false

      manager.launchBoundControlTask(resource) {
         try {
            running1 = true
            withContext(NonCancellable) {
               latch.await()
            }
         } finally {
            running1 = false
         }
      }

      runCurrent()

      manager.launchBoundControlTask(resource) {
         try {
            running2 = true
            awaitCancellation()
         } finally {
            running2 = false
         }
      }

      runCurrent()

      running1 shouldBe true
      running2 shouldBe false

      latch.complete(Unit)
      runCurrent()

      running1 shouldBe false
      running2 shouldBe true
   }

   @Test
   internal fun `launchResourceControlTask should inherit cancellation from launchBoundControlTask`() = scope.runTest {
      val resourceA = MutableStateFlow<Outcome<Unit>>(Outcome.Progress())
      val resourceB = MutableStateFlow<Outcome<Unit>>(Outcome.Progress())

      var runningA1 = false
      var runningA2 = false
      var runningB = false

      manager.launchResourceControlTask(resourceA) {
         try {
            runningA1 = true
            awaitCancellation()
         } finally {
            runningA1 = false
         }
      }

      manager.launchResourceControlTask(resourceB) {
         try {
            runningB = true
            awaitCancellation()
         } finally {
            runningB = false
         }
      }

      runCurrent()

      manager.launchBoundControlTask(resourceA) {
         try {
            runningA2 = true
            awaitCancellation()
         } finally {
            runningA2 = false
         }
      }

      runCurrent()

      runningA1 shouldBe false
      runningA2 shouldBe true
      runningB shouldBe true
   }

   @Test
   internal fun `launchResourceControlTask should automatically put the flow into Progress`() = scope.runTest {
      val resource = MutableStateFlow<Outcome<Unit>>(Outcome.Error(NoNetworkException()))

      manager.launchResourceControlTask(resource) {
         // Do nothing
      }

      runCurrent()

      resource.value shouldBeProgressWithData null
   }

   @Test
   internal fun `launchResourceControlTask should keep previous data`() = scope.runTest {
      val resource = MutableStateFlow<Outcome<Int>>(Outcome.Success(12))

      manager.launchResourceControlTask(resource) {
         // Do nothing
      }

      runCurrent()

      resource.value shouldBeProgressWithData 12
   }

   @Test
   internal fun `launchResourceControlTask should keep catch and report cause exceptions inside block`() = scope.runTest {
      val resource = MutableStateFlow<Outcome<Int>>(Outcome.Success(12))

      manager.launchResourceControlTask(resource) {
         throw NoNetworkException()
      }

      runCurrent()

      resource.value.shouldBeErrorWith(exceptionType = NoNetworkException::class.java)
      reportedErrors.shouldHaveSize(1).first().shouldBeInstanceOf<NoNetworkException>()
   }

   @Test
   internal fun `launchResourceControlTask should keep catch and report unknown exceptions inside block`() = scope.runTest {
      val resource = MutableStateFlow<Outcome<Int>>(Outcome.Success(12))

      val exception = IndexOutOfBoundsException()

      manager.launchResourceControlTask(resource) {
         throw exception
      }

      runCurrent()

      val value = resource.value
      value.shouldBeInstanceOf<Outcome.Error<*>>()
         .exception.let {
            it.shouldBeInstanceOf<UnknownCauseException>()
            it.cause shouldBeSameInstanceAs exception
         }

      reportedErrors.shouldHaveSize(1).first().shouldBeInstanceOf<IndexOutOfBoundsException>()
   }

   @Test
   internal fun `launchResourceControlTask should report exceptions inside flows`() = scope.runTest {
      val resource = MutableStateFlow<Outcome<Int>>(Outcome.Success(12))

      manager.launchResourceControlTask(resource) {
         emitAll(flowOf(Outcome.Error(NoNetworkException())))
      }

      runCurrent()

      reportedErrors.shouldHaveSize(1).first().shouldBeInstanceOf<NoNetworkException>()
   }

   @Test
   internal fun `launchResourceControlTask should emit data to the target flow`() = scope.runTest {
      val resource = MutableStateFlow<Outcome<Int>>(Outcome.Success(12))

      resource.test {
         manager.launchResourceControlTask(resource) {
            emitAll(flowOf(Outcome.Success(3), Outcome.Progress(4), Outcome.Success(5)))
         }

         awaitItem() shouldBe Outcome.Success(12)
         awaitItem() shouldBe Outcome.Progress(12)
         awaitItem() shouldBe Outcome.Success(3)
         awaitItem() shouldBe Outcome.Progress(4)
         awaitItem() shouldBe Outcome.Success(5)
      }
   }

   @Test
   internal fun `launchResourceControlTask should emit data to the target flow when launching`() = scope.runTest {
      val resource = MutableStateFlow<Outcome<Int>>(Outcome.Success(12))

      resource.test {
         manager.launchResourceControlTask(resource) {
            launchAndEmitAll(flowOf(Outcome.Success(3), Outcome.Progress(4), Outcome.Success(5)))
         }

         awaitItem() shouldBe Outcome.Success(12)
         awaitItem() shouldBe Outcome.Progress(12)
         awaitItem() shouldBe Outcome.Success(3)
         awaitItem() shouldBe Outcome.Progress(4)
         awaitItem() shouldBe Outcome.Success(5)
      }
   }

   @Test
   internal fun `Report failures in the reportError call`() = scope.runTest {
      val exception = NoNetworkException()

      manager.reportError(Outcome.Error<Unit>(exception))

      reportedErrors.shouldContainExactly(exception)
   }

   @Test
   internal fun `Ignore non-error Outcomes in the reportError call`() = scope.runTest {
      manager.reportError(Outcome.Progress<Unit>())
      manager.reportError(Outcome.Success(Unit))

      reportedErrors.shouldBeEmpty()
   }

   @Test
   internal fun `launchResourceControlTask should keep old data by default when exception is thrown`() = scope.runTest {
      val resource = MutableStateFlow<Outcome<Int>>(Outcome.Success(12))

      manager.launchResourceControlTask(resource) {
         emit(Outcome.Success(18))
         throw NoNetworkException()
      }

      runCurrent()

      resource.value.shouldBeErrorWith(
         expectedData = 18,
         exceptionType = NoNetworkException::class.java,
      )
      reportedErrors.shouldHaveSize(1).first().shouldBeInstanceOf<NoNetworkException>()
   }

   @Test
   internal fun `launchResourceControlTask should not keep old data when exception is thrown when disabled`() = scope.runTest {
      val resource = MutableStateFlow<Outcome<Int>>(Outcome.Success(12))

      manager.launchResourceControlTask(resource, keepDataOnExceptions = false) {
         emit(Outcome.Success(18))
         throw NoNetworkException()
      }

      runCurrent()

      resource.value.shouldBeErrorWith(
         expectedData = null,
         exceptionType = NoNetworkException::class.java,
      )
      reportedErrors.shouldHaveSize(1).first().shouldBeInstanceOf<NoNetworkException>()
   }

   @Test
   internal fun `launchWithExceptionReporting report exceptions that occur inside block`() = scope.runTest {
      manager.launchWithExceptionReporting {
         throw NoNetworkException()
      }

      runCurrent()

      reportedErrors.shouldHaveSize(1).first().shouldBeInstanceOf<NoNetworkException>()
   }
}
