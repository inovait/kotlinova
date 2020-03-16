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

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author Matej Drobnic
 */
class GmsCoroutinesTest {
    @Test
    fun testTaskAwaitResult1() = runBlocking {
        val task = Tasks.forResult("SUCCESS")
        assertEquals("SUCCESS", task.await())
    }

    @Test
    fun testTaskAwaitResult2() = runBlocking {
        val task = TaskCompletionSource<String>()
        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val result = async(Dispatchers.Unconfined) { task.task.await() }

        task.setResult("SUCCESS")

        assertEquals("SUCCESS", result.await())
    }

    @Test(expected = NoSuchElementException::class)
    fun testTaskAwaitException1() = runBlocking<Unit> {
        val task = Tasks.forException<String>(NoSuchElementException())

        task.await()
    }

    @Test(expected = NoSuchElementException::class)
    fun testTaskAwaitException2() = runBlocking<Unit> {
        val task = TaskCompletionSource<String>()
        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val result = async(Dispatchers.Unconfined) { task.task.await() }

        task.setException(NoSuchElementException())

        result.await()
    }
}