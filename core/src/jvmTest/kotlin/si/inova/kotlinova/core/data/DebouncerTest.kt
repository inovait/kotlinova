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

package si.inova.kotlinova.core.data

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.time.virtualTimeProvider

internal class DebouncerTest {

   @Test
   fun `Wait for task to start when not in immediate mode`() = runTest {
      advanceTimeBy(100_000)

      val debouncer = Debouncer(backgroundScope, virtualTimeProvider())

      val task = ExecutableTask()

      debouncer.executeDebouncing(task = task)
      runCurrent()
      task.called shouldBe false

      advanceTimeBy(600)
      task.called shouldBe true
   }

   @Test
   fun `Start task immediately in immediate mode`() = runTest {
      advanceTimeBy(100_000)

      val debouncer = Debouncer(backgroundScope, virtualTimeProvider(), triggerFirstImmediately = true)

      val task = ExecutableTask()

      debouncer.executeDebouncing(task = task)
      runCurrent()
      task.called shouldBe true
   }

   @Test
   fun `Start task immediately in immediate mode with time being zero`() = runTest {
      val debouncer = Debouncer(backgroundScope, virtualTimeProvider(), triggerFirstImmediately = true)

      val task = ExecutableTask()

      debouncer.executeDebouncing(task = task)
      runCurrent()
      task.called shouldBe true
   }

   @Test
   fun `Cancel previous task when triggering new one`() = runTest {
      advanceTimeBy(100_000)

      val debouncer = Debouncer(backgroundScope, virtualTimeProvider())

      val task = ExecutableTask()

      debouncer.executeDebouncing(task = task)
      advanceTimeBy(600)

      debouncer.executeDebouncing { }
      runCurrent()
      task.cancelled shouldBe true
   }

   @Test
   fun `Do not start new task until debouncing period is over`() = runTest {
      advanceTimeBy(100_000)

      val debouncer = Debouncer(backgroundScope, virtualTimeProvider())

      debouncer.executeDebouncing {}
      advanceTimeBy(200)

      val task = ExecutableTask()
      debouncer.executeDebouncing(task = task)
      runCurrent()
      task.called shouldBe false

      advanceTimeBy(600)
      task.called shouldBe true
   }

   @Test
   fun `Do not start new task until debouncing period is over even in immediate mode`() = runTest {
      advanceTimeBy(100_000)

      val debouncer = Debouncer(backgroundScope, virtualTimeProvider(), triggerFirstImmediately = true)

      debouncer.executeDebouncing {}
      advanceTimeBy(200)

      val task = ExecutableTask()
      debouncer.executeDebouncing(task = task)
      runCurrent()
      task.called shouldBe false

      advanceTimeBy(600)
      task.called shouldBe true
   }

   @Test
   fun `Do not start any task if debouncing period keeps changing`() = runTest {
      advanceTimeBy(100_000)

      val debouncer = Debouncer(backgroundScope, virtualTimeProvider())

      debouncer.executeDebouncing {}
      advanceTimeBy(600)

      val task = ExecutableTask()
      debouncer.executeDebouncing(task = task)
      runCurrent()

      advanceTimeBy(100)
      debouncer.executeDebouncing(task = task)
      advanceTimeBy(100)
      debouncer.executeDebouncing(task = task)
      advanceTimeBy(100)
      debouncer.executeDebouncing(task = task)
      advanceTimeBy(100)
      debouncer.executeDebouncing(task = task)

      task.called shouldBe false
   }

   @Test
   fun `Wait for task to start when not in immediate mode with custom delay`() = runTest {
      advanceTimeBy(100_000)

      val debouncer = Debouncer(backgroundScope, virtualTimeProvider())

      val task = ExecutableTask()

      debouncer.executeDebouncing(task = task, debouncingTimeMs = 300L)
      runCurrent()
      task.called shouldBe false

      advanceTimeBy(300)
      runCurrent()
      task.called shouldBe true
   }

   private class ExecutableTask : suspend () -> Unit {
      var called = false
      var cancelled = false

      override suspend fun invoke() {
         try {
            called = true
            awaitCancellation()
         } finally {
            cancelled = true
         }
      }
   }
}
