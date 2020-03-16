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

        Espresso.onIdle()

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

            Espresso.onIdle()
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