package si.inova.kotlinova.data

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import si.inova.kotlinova.testing.LocalFunction0
import java.util.concurrent.TimeUnit

/**
 * @author Matej Drobnic
 */
@Ignore
@RunWith(RobolectricTestRunner::class)
class DebouncerTest {
    private lateinit var debouncer: Debouncer

    @Before
    fun init() {
        debouncer = Debouncer()
    }

    @Test
    fun testSingleTask() {
        val task: LocalFunction0<Unit> = mock()
        whenever(task.invoke()).thenReturn(Unit)
        val taskInorder = inOrder(task)

        debouncer.executeDebouncing(task)
        taskInorder.verify(task, never()).invoke()

        Robolectric.getForegroundThreadScheduler().advanceBy(600, TimeUnit.MILLISECONDS)
        taskInorder.verify(task).invoke()

        Robolectric.getForegroundThreadScheduler().advanceBy(600, TimeUnit.HOURS)
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
        inOrderTest.verify(taskA, never()).invoke()

        Robolectric.getForegroundThreadScheduler().advanceBy(400, TimeUnit.MILLISECONDS)
        inOrderTest.verify(taskA, never()).invoke()

        debouncer.executeDebouncing(taskB)
        inOrderTest.verify(taskB, never()).invoke()

        Robolectric.getForegroundThreadScheduler().advanceBy(400, TimeUnit.MILLISECONDS)
        inOrderTest.verify(taskB, never()).invoke()

        Robolectric.getForegroundThreadScheduler().advanceBy(500, TimeUnit.MILLISECONDS)
        inOrderTest.verify(taskB).invoke()
        Robolectric.getForegroundThreadScheduler().advanceBy(600, TimeUnit.HOURS)
        inOrderTest.verifyNoMoreInteractions()
    }
}