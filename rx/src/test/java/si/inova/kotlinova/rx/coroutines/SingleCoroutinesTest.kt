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

package si.inova.kotlinova.rx.coroutines

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit

class SingleCoroutinesTest {
    @get:Rule
    val rule = Timeout(1, TimeUnit.SECONDS)

    @Test
    fun awaitSingle() = runBlocking {
        val single = Single.just(32)

        assertEquals(32, single.await())
    }

    @Test(expected = IllegalStateException::class)
    fun awaitSingleThrowErrors() = runBlocking<Unit> {
        val single = Single.error<Int>(IllegalStateException())

        single.await()
    }

    @Test
    fun awaitSingleValueArrivingWhileWaiting() = runBlocking {
        val subject = PublishSubject.create<Int>()
        val single = subject.firstOrError()

        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val awaitTask = async(Dispatchers.Unconfined) {
            single.await()
        }

        assertFalse(awaitTask.isCompleted)
        assertTrue(subject.hasObservers())
        subject.onNext(12)

        assertTrue(awaitTask.isCompleted)
        assertEquals(12, awaitTask.await())
    }

    @Test
    fun awaitSingleUnsubscribeWhenCancellingTask() = runBlocking {
        val subject = PublishSubject.create<Int>()
        val single = subject.firstOrError()

        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val awaitTask = async(Dispatchers.Unconfined) {
            single.await()
        }

        assertFalse(awaitTask.isCompleted)
        assertTrue(subject.hasObservers())

        awaitTask.cancel()
        assertFalse(subject.hasObservers())
    }
}