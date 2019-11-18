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