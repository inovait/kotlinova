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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.ImmediateDispatcherRule
import si.inova.kotlinova.testing.LocalFunction0
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import si.inova.kotlinova.utils.map

/**
 * @author Matej Drobnic
 */
class LiveDataCoroutinesTest {
    @get:Rule
    val threadErrorRule = UncaughtExceptionThrowRule()

    @get:Rule
    val dispatcher = ImmediateDispatcherRule()

    @get:Rule
    val archRule = InstantTaskExecutorRule()

    @Test
    fun testAwaitFirstValue() = runBlocking<Unit> {
        val data = MutableLiveData<Int>()
        assertFalse(data.hasActiveObservers())
        val task = GlobalScope.async(
                Dispatchers.Main,
                CoroutineStart.DEFAULT, { data.awaitFirstValue() })

        assertFalse(task.isCompleted)
        assertTrue(data.hasActiveObservers())

        data.value = 10
        assertTrue(task.isCompleted)
        assertEquals(10, task.await())
        assertFalse(data.hasActiveObservers())
    }

    @Test
    fun testAwaitFirstValueWithBlock() = runBlocking<Unit> {
        val data = MutableLiveData<Int>()
        val testBlock: LocalFunction0<Unit> = mock()
        data.value = 10
        data.awaitFirstValue(runAfterObserve = testBlock)

        verify(testBlock).invoke()
    }

    @Test
    fun testAwaitFirstValueAllowExisting() = runBlocking<Unit> {
        val data = MutableLiveData<Int>()

        data.value = 10
        val task = GlobalScope.async(
                Dispatchers.Main,
                CoroutineStart.DEFAULT,
                { data.awaitFirstValue(ignoreExistingValue = false) })
        data.value = 20

        assertEquals(10, task.await())
    }

    @Test
    fun testAwaitFirstValueIgnoreExisting() = runBlocking<Unit> {
        val data = MutableLiveData<Int>()

        data.value = 10
        val task = GlobalScope.async(Dispatchers.Main, CoroutineStart.DEFAULT, {
            data.awaitFirstValue(ignoreExistingValue = true, runAfterObserve = {
                data.value = 20
            })
        })

        assertEquals(20, task.await())
    }

    @Test
    fun testAwaitFirstValueIgnoreExistingThroughMediator() = runBlocking<Unit> {
        val data = MutableLiveData<Int>()

        // Wrap around MediatorLiveData
        val mappedData = data.map { it!! * 5 }

        data.value = 10
        val task = GlobalScope.async(Dispatchers.Main, CoroutineStart.DEFAULT, {
            mappedData.awaitFirstValue(ignoreExistingValue = true, runAfterObserve = {
                data.value = 20
            })
        })

        assertEquals(100, task.await())
    }

    @Test
    fun testAwaitFirstValueIgnoreExistingSameValue() = runBlocking<Unit> {
        val data = MutableLiveData<Int>()

        data.value = 10
        val task = GlobalScope.async(
                Dispatchers.Main,
                CoroutineStart.DEFAULT,
                { data.awaitFirstValue(ignoreExistingValue = true) })
        assertFalse(task.isCompleted)
        data.value = 10

        assertTrue(task.isCompleted)
        assertEquals(10, task.await())
    }

    @Test
    fun testAwaitFirstValueCallbacks() {
        val data = spy(MutableLiveData<Int>())

        val afterSubscribeCallback: LocalFunction0<Unit> = mock()
        val beforeUnsubscribeCallback: LocalFunction0<Unit> = mock()

        GlobalScope.async(Dispatchers.Main, CoroutineStart.DEFAULT, {
            data.awaitFirstValue(
                    runAfterObserve = afterSubscribeCallback,
                    runAfterCompletionBeforeRemoveObserver = beforeUnsubscribeCallback
            )
        })

        inOrder(data, afterSubscribeCallback, beforeUnsubscribeCallback) {
            verify(data).observeForever(any())
            verify(afterSubscribeCallback).invoke()
            verifyNoMoreInteractions()

            data.value = 10

            verify(beforeUnsubscribeCallback).invoke()
            verify(data).removeObserver(any())
        }
    }
}