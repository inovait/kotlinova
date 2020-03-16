/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.data

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.CoroutinesTimeMachine
import si.inova.kotlinova.testing.LocalFunction0

/**
 * @author Matej Drobnic
 */
class DebouncerTest {
    @get:Rule
    val scheduler = CoroutinesTimeMachine()

    private val debouncer = Debouncer()

    @Test
    fun testSingleTask() {
        val task: LocalFunction0<Unit> = mock()
        whenever(task.invoke()).thenReturn(Unit)
        val taskInorder = inOrder(task)

        debouncer.executeDebouncing(task)
        scheduler.triggerActions()
        taskInorder.verify(task, never()).invoke()

        scheduler.advanceTime(600)
        taskInorder.verify(task).invoke()

        scheduler.advanceTime(9999999)
        taskInorder.verifyNoMoreInteractions()
    }

    @Test
    fun testTwoTasks() {
        val taskA: LocalFunction0<Unit> = mock()
        whenever(taskA.invoke()).thenReturn(Unit)
        val taskB: LocalFunction0<Unit> = mock()
        whenever(taskB.invoke()).thenReturn(Unit)

        val inOrderTest = inOrder(taskA, taskB)

        debouncer.executeDebouncing(taskA)
        scheduler.triggerActions()
        inOrderTest.verify(taskA, never()).invoke()

        scheduler.advanceTime(400)
        inOrderTest.verify(taskA, never()).invoke()

        debouncer.executeDebouncing(taskB)
        scheduler.triggerActions()
        inOrderTest.verify(taskB, never()).invoke()

        scheduler.advanceTime(400)
        inOrderTest.verify(taskB, never()).invoke()

        scheduler.advanceTime(500)
        inOrderTest.verify(taskB).invoke()

        scheduler.advanceTime(600)
        inOrderTest.verifyNoMoreInteractions()
    }
}