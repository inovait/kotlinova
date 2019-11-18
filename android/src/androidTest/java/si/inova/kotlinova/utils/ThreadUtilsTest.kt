package si.inova.kotlinova.utils

import android.os.Looper
import androidx.test.espresso.Espresso
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import kotlinx.coroutines.Job
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.LocalFunction0
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import kotlin.concurrent.thread

class ThreadUtilsTest {
    @get:Rule
    val errorRule = UncaughtExceptionThrowRule()

    @Test
    fun runOnUiThreadWhenOnUiThread() {
        val task: LocalFunction0<Unit> = mock()

        runOnUiThread {
            assertTrue(Looper.myLooper() == Looper.getMainLooper())
            task()
        }

        verify(task).invoke()
    }

    @Test
    fun runOnUiThreadWhenOnSeparateThread() {
        val task: LocalFunction0<Unit> = mock()

        thread {
            runOnUiThread {
                assertTrue(Looper.myLooper() == Looper.getMainLooper())
                task()
            }
        }.join()

        verify(task).invoke()
    }

    @Test
    fun cancelRunOnUiThreadWhenOnSeparateThread() {
        val task: LocalFunction0<Unit> = mock()

        val job = Job()
        job.cancel()

        thread {
            runOnUiThread(parentJob = job, block = task)
        }.join()

        Espresso.onIdle()

        verifyZeroInteractions(task)
    }
}