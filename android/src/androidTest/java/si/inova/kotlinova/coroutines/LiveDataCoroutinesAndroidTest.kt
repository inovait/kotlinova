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

package si.inova.kotlinova.coroutines

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule

/**
 * @author Matej Drobnic
 */
class LiveDataCoroutinesAndroidTest {
    @get:Rule
    val threadErrorRule = UncaughtExceptionThrowRule()

    @Test
    fun testCancelAwaitFirstValueOnSeparateThread() = runBlocking {
        val data = MutableLiveData<Int>()

        @Suppress("EXPERIMENTAL_API_USAGE")
        val thread = newSingleThreadContext("Test thread")
        thread.use {
            withContext(thread) {
                val task = async(thread) {
                    data.awaitFirstValue()
                }

                // Block a bit until another thread does the task
                for (i in 0 until 200) {
                    if (data.hasActiveObservers()) {
                        break
                    }

                    yield()
                }

                withContext(Dispatchers.Main) {
                    assertTrue(data.hasActiveObservers())
                }

                task.cancel()

                // Block a bit until another thread does the task
                for (i in 0 until 200) {
                    if (!data.hasActiveObservers()) {
                        break
                    }

                    yield()
                }

                withContext(Dispatchers.Main) {
                    assertFalse(data.hasActiveObservers())
                }
            }
        }
    }
}