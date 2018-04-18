package si.inova.kotlinova.utils

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import si.inova.kotlinova.testing.LocalFunction0
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import kotlin.concurrent.thread

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
class ThreadUtilsTest {
    @get:Rule
    val errorRule = UncaughtExceptionThrowRule()

    @Test
    fun runOnUiThreadWhenOnUiThread() {
        Robolectric.getForegroundThreadScheduler().pause()

        val task: LocalFunction0<Unit> = mock()

        runOnUiThread(task)

        verify(task).invoke()
    }

    @Test
    fun runOnUiThreadWhenOnSeparateThread() {
        Robolectric.getForegroundThreadScheduler().pause()
        val task: LocalFunction0<Unit> = mock()

        thread {

            runOnUiThread(task)
        }.join()

        inOrder(task) {
            verifyNoMoreInteractions()

            Robolectric.getForegroundThreadScheduler().unPause()

            verify(task).invoke()
        }
    }
}