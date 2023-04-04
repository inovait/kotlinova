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
      val debouncer = Debouncer(backgroundScope, virtualTimeProvider())

      val task = ExecutableTask()

      debouncer.executeDebouncing(task)
      runCurrent()
      task.called shouldBe false

      advanceTimeBy(600)
      task.called shouldBe true
   }

   @Test
   fun `Start task immediately in immediate mode`() = runTest {
      val debouncer = Debouncer(backgroundScope, virtualTimeProvider(), triggerFirstImmediately = true)

      val task = ExecutableTask()

      debouncer.executeDebouncing(task)
      runCurrent()
      task.called shouldBe true
   }

   @Test
   fun `Cancel previous task when triggering new one`() = runTest {
      val debouncer = Debouncer(backgroundScope, virtualTimeProvider())

      val task = ExecutableTask()

      debouncer.executeDebouncing(task)
      advanceTimeBy(600)

      debouncer.executeDebouncing { }
      runCurrent()
      task.cancelled shouldBe true
   }

   @Test
   fun `Do not start new task until debouncing period is over`() = runTest {
      val debouncer = Debouncer(backgroundScope, virtualTimeProvider())

      debouncer.executeDebouncing {}
      advanceTimeBy(200)

      val task = ExecutableTask()
      debouncer.executeDebouncing(task)
      runCurrent()
      task.called shouldBe false

      advanceTimeBy(600)
      task.called shouldBe true
   }

   @Test
   fun `Do not start new task until debouncing period is over even in immediate mode`() = runTest {
      val debouncer = Debouncer(backgroundScope, virtualTimeProvider(), triggerFirstImmediately = true)

      debouncer.executeDebouncing {}
      advanceTimeBy(200)

      val task = ExecutableTask()
      debouncer.executeDebouncing(task)
      runCurrent()
      task.called shouldBe false

      advanceTimeBy(600)
      task.called shouldBe true
   }

   @Test
   fun `Do not start any task if debouncing period keeps changing`() = runTest {
      val debouncer = Debouncer(backgroundScope, virtualTimeProvider())

      debouncer.executeDebouncing {}
      advanceTimeBy(600)

      val task = ExecutableTask()
      debouncer.executeDebouncing(task)
      runCurrent()

      advanceTimeBy(100)
      debouncer.executeDebouncing(task)
      advanceTimeBy(100)
      debouncer.executeDebouncing(task)
      advanceTimeBy(100)
      debouncer.executeDebouncing(task)
      advanceTimeBy(100)
      debouncer.executeDebouncing(task)

      task.called shouldBe false
   }

//
//   @Test
//   fun testTwoTasks() {
//      val taskA: LocalFunction0<Unit> = mock()
//      whenever(taskA.invoke()).thenReturn(Unit)
//      val taskB: LocalFunction0<Unit> = mock()
//      whenever(taskB.invoke()).thenReturn(Unit)
//
//      val inOrderTest = inOrder(taskA, taskB)
//
//      debouncer.executeDebouncing(taskA)
//      scheduler.triggerActions()
//      inOrderTest.verify(taskA, never()).invoke()
//
//      scheduler.advanceTime(400)
//      inOrderTest.verify(taskA, never()).invoke()
//
//      debouncer.executeDebouncing(taskB)
//      scheduler.triggerActions()
//      inOrderTest.verify(taskB, never()).invoke()
//
//      scheduler.advanceTime(400)
//      inOrderTest.verify(taskB, never()).invoke()
//
//      scheduler.advanceTime(500)
//      inOrderTest.verify(taskB).invoke()
//
//      scheduler.advanceTime(600)
//      inOrderTest.verifyNoMoreInteractions()
//   }

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
