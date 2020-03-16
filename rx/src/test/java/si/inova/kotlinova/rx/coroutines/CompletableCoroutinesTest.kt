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

import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit

class CompletableCoroutinesTest {
    @get:Rule
    val rule = Timeout(1, TimeUnit.SECONDS)

    @Test
    fun awaitCompletable() = runBlocking {
        val subject = PublishSubject.create<Int>()
        val completable = subject.ignoreElements()

        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val awaitTask = async(Dispatchers.Unconfined) {
            completable.await()
        }

        assertFalse(awaitTask.isCompleted)
        assertTrue(subject.hasObservers())
        subject.onComplete()

        assertTrue(awaitTask.isCompleted)
    }

    @Test(expected = IllegalStateException::class)
    fun awaitCompletableThrowErrors() = runBlocking<Unit> {
        val completable = Completable.error(IllegalStateException())

        completable.await()
    }

    @Test
    fun awaitCompletableUnsubscribeWhenCancellingTask() = runBlocking {
        val subject = PublishSubject.create<Int>()
        val completable = subject.ignoreElements()

        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val awaitTask = async(Dispatchers.Unconfined) {
            completable.await()
        }

        assertFalse(awaitTask.isCompleted)
        assertTrue(subject.hasObservers())

        awaitTask.cancel()
        assertFalse(subject.hasObservers())
    }
}