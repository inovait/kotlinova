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

package si.inova.kotlinova.rx

import com.nhaarman.mockitokotlin2.*
import io.reactivex.functions.Consumer
import kotlinx.coroutines.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.rx.testing.RxCoroutinesTimeMachine
import si.inova.kotlinova.testing.RxSchedulerRule
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule

/**
 * @author Matej Drobnic
 */
class OnDemandProviderTest {
    private lateinit var testProvider: TestProvider
    private lateinit var onActiveCallback: () -> Unit
    private lateinit var onInactiveCallback: () -> Unit

    @get:Rule
    val dispatcher = RxCoroutinesTimeMachine()

    @get:Rule
    val rxJavaSchedulerRule = RxSchedulerRule()

    @get:Rule
    val uncaughtExceptionThrowRule = UncaughtExceptionThrowRule()

    @Before
    fun setUp() {
        createTestProvider(true)
    }

    @Test
    fun ActiveInactive() {
        // Debounce is not yet tested here
        createTestProvider(debounce = false)

        inOrder(onActiveCallback, onInactiveCallback) {
            runBlocking {
                verifyNoMoreInteractions()

                val subscriber = testProvider.flowable.subscribe()
                dispatcher.triggerActions()
                verify(onActiveCallback).invoke()

                subscriber.dispose()
                dispatcher.triggerActions()
                verify(onInactiveCallback).invoke()

                verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun ActiveInactiveMultipleSubscribers() {
        // Debounce is not yet tested here
        createTestProvider(debounce = false)

        inOrder(onActiveCallback, onInactiveCallback) {
            runBlocking {
                verifyNoMoreInteractions()

                val subscriberA = testProvider.flowable.subscribe()
                val subscriberB = testProvider.flowable.subscribe()
                dispatcher.triggerActions()
                verify(onActiveCallback, times(1)).invoke()

                subscriberA.dispose()
                dispatcher.triggerActions()
                verifyNoMoreInteractions()
                subscriberB.dispose()

                dispatcher.triggerActions()
                verify(onInactiveCallback).invoke()

                dispatcher.triggerActions()
                verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun isActive() {
        // Debounce is not yet tested here
        createTestProvider(debounce = false)

        dispatcher.triggerActions()
        assertFalse(testProvider.isActive)

        val subscriberA = testProvider.flowable.subscribe()
        val subscriberB = testProvider.flowable.subscribe()
        dispatcher.triggerActions()
        assertTrue(testProvider.isActive)

        subscriberA.dispose()
        dispatcher.triggerActions()
        assertTrue(testProvider.isActive)

        subscriberB.dispose()
        dispatcher.triggerActions()
        assertFalse(testProvider.isActive)
    }

    @Test
    fun receiveData() {
        runBlocking {
            val consumer: Consumer<Int> = mock()
            val subscriber = testProvider.flowable.subscribe(consumer)
            dispatcher.triggerActions()

            inOrder(consumer) {
                runBlocking {
                    testProvider.sendPublic(1)
                    dispatcher.triggerActions()
                    verify(consumer).accept(1)
                    testProvider.sendPublic(20)
                    dispatcher.triggerActions()
                    verify(consumer).accept(20)
                    testProvider.sendPublic(33)
                    dispatcher.triggerActions()
                    verify(consumer).accept(33)
                    testProvider.sendPublic(45)
                    dispatcher.triggerActions()
                    verify(consumer).accept(45)

                    subscriber.dispose()

                    dispatcher.triggerActions()
                    verifyNoMoreInteractions()
                }
            }
        }
    }

    @Test
    fun receiveCrashExceptions() {
        // Debounce is not yet tested here
        createTestProvider(debounce = false)

        runBlocking {
            val exception = RuntimeException("Test")
            val consumer: Consumer<Int> = mock()
            val errorConsumer: Consumer<in Throwable> = mock()
            whenever(onActiveCallback.invoke()).thenThrow(exception)

            val subscriber = testProvider.flowable.subscribe(consumer, errorConsumer)

            dispatcher.triggerActions()
            verify(errorConsumer).accept(exception)
            verify(onInactiveCallback).invoke()
            assertTrue(subscriber.isDisposed)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun crashIfEmittingBeforeDispose() = runBlocking {
        testProvider.sendPublic(10)
        dispatcher.triggerActions()
    }

    @Test(expected = IllegalStateException::class)
    fun crashIfEmittingAfterDispose() = runBlocking {
        // Debounce is not yet tested here
        createTestProvider(debounce = false)

        val subscriber = testProvider.flowable.subscribe()
        dispatcher.triggerActions()
        testProvider.sendPublic(20)
        subscriber.dispose()

        testProvider.sendPublic(10)
        dispatcher.triggerActions()
    }

    @Test
    fun cancelOnActiveAfterDispose() {
        val afterDelay: () -> Unit = mock()

        val provider = object : OnDemandProvider<Int>(debounceTimeout = 0) {
            override suspend fun CoroutineScope.onActive() {
                delay(500)
                afterDelay()
            }
        }

        provider.flowable.subscribe().dispose()
        dispatcher.advanceTime(1000)

        verifyZeroInteractions(afterDelay)
    }

    @Test
    fun activeInactiveWithDebounce() {
        inOrder(onActiveCallback, onInactiveCallback) {
            runBlocking {
                verifyNoMoreInteractions()

                val subscriber = testProvider.flowable.subscribe()
                dispatcher.triggerActions()
                verify(onActiveCallback).invoke()

                subscriber.dispose()
                dispatcher.triggerActions()
                verifyNoMoreInteractions()

                dispatcher.advanceTime(200)
                verifyNoMoreInteractions()

                dispatcher.advanceTime(600)

                verify(onInactiveCallback).invoke()

                verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun isActiveWithDebounce() {
        assertFalse(testProvider.isActive)

        val subscriber = testProvider.flowable.subscribe()
        dispatcher.triggerActions()
        assertTrue(testProvider.isActive)

        subscriber.dispose()
        dispatcher.triggerActions()
        assertTrue(testProvider.isActive)

        dispatcher.advanceTime(200)
        assertTrue(testProvider.isActive)

        dispatcher.advanceTime(600)

        assertFalse(testProvider.isActive)
    }

    @Test
    fun resubscribeWithDebounce() {
        inOrder(onActiveCallback, onInactiveCallback) {
            runBlocking {
                dispatcher.triggerActions()
                verifyNoMoreInteractions()

                var subscriber = testProvider.flowable.subscribe()
                dispatcher.triggerActions()
                verify(onActiveCallback).invoke()

                subscriber.dispose()
                dispatcher.triggerActions()
                verifyNoMoreInteractions()

                dispatcher.advanceTime(200)
                dispatcher.triggerActions()
                verifyNoMoreInteractions()

                subscriber = testProvider.flowable.subscribe()

                dispatcher.advanceTime(2000)

                dispatcher.triggerActions()
                verifyNoMoreInteractions()

                subscriber.dispose()
                dispatcher.advanceTime(200)

                dispatcher.triggerActions()
                verifyNoMoreInteractions()

                dispatcher.advanceTime(2000)

                dispatcher.triggerActions()
                verify(onInactiveCallback).invoke()
                verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun doNotRepeatOnInactiveWhenWithinDebounce() {
        inOrder(onActiveCallback, onInactiveCallback) {
            runBlocking {

                val subscriber = testProvider.flowable.subscribe()
                dispatcher.triggerActions()
                verify(onActiveCallback).invoke()

                subscriber.dispose()

                dispatcher.advanceTime(200)

                testProvider.flowable.subscribe()

                dispatcher.triggerActions()
                verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun receiveValueTransmittedWhenInDebounce() {
        val consumer: Consumer<Int> = mock()

        runBlocking {
            val subscriber = testProvider.flowable.subscribe()
            dispatcher.triggerActions()
            subscriber.dispose()

            dispatcher.advanceTime(100)
            testProvider.sendPublic(15)
            dispatcher.advanceTime(100)

            testProvider.flowable.subscribe(consumer)
            dispatcher.triggerActions()
            verify(consumer).accept(15)
        }
    }

    @Test
    fun receiveValueTransmittedAfterDebounce() {
        val consumer: Consumer<Int> = mock()

        runBlocking {
            val subscriber = testProvider.flowable.subscribe()
            dispatcher.triggerActions()
            subscriber.dispose()

            dispatcher.advanceTime(100)

            testProvider.flowable.subscribe(consumer)

            testProvider.sendPublic(15)
            dispatcher.triggerActions()
            verify(consumer).accept(15)
        }
    }

    @Test
    fun cancelOnActiveAfterDisposeWithDebounce() {
        val afterDelay: () -> Unit = mock()

        val provider = object : OnDemandProvider<Int>() {
            override suspend fun CoroutineScope.onActive() {
                delay(1000)
                afterDelay()
            }
        }

        provider.flowable.subscribe().dispose()
        dispatcher.advanceTime(2000)

        verifyZeroInteractions(afterDelay)
    }

    @Test
    fun waitForCleanupToFinishBeforeRestart() {
        val cleanupFinish: () -> Unit = mock()
        val onActiveStart: () -> Unit = mock()

        val provider = object : OnDemandProvider<Unit>() {
            override suspend fun CoroutineScope.onActive() {
                onActiveStart()
            }

            override fun onInactive() {
                cleanupFinish()
            }
        }

        inOrder(cleanupFinish, onActiveStart) {
            val subscriber = provider.flowable.subscribe()
            dispatcher.triggerActions()
            verify(onActiveStart).invoke()

            subscriber.dispose()
            dispatcher.triggerActions()
            verifyNoMoreInteractions()

            dispatcher.advanceTime(600)
            provider.flowable.subscribe()

            dispatcher.advanceTime(2000)

            verify(cleanupFinish).invoke()
            verify(onActiveStart).invoke()
        }
    }

    @Test
    fun waitForStartupToFinishBeforeCleanup() {
        val startupFinish: () -> Unit = mock()
        val onInactiveStart: () -> Unit = mock()

        val provider = object : OnDemandProvider<Unit>() {
            override suspend fun CoroutineScope.onActive() {
                withContext(NonCancellable) {
                    delay(1000)
                    startupFinish()
                }
            }

            override fun onInactive() {
                onInactiveStart()
            }
        }

        inOrder(startupFinish, onInactiveStart) {
            val subscriber = provider.flowable.subscribe()
            dispatcher.triggerActions()

            subscriber.dispose()

            dispatcher.advanceTime(2000)

            verify(startupFinish).invoke()
            verify(onInactiveStart).invoke()
        }
    }

    @Test
    fun callCleanupAfterLongJob() {
        val cleanupCalled: () -> Unit = mock()

        val provider = object : OnDemandProvider<Unit>() {
            override suspend fun CoroutineScope.onActive() {
                delay(99999999)
            }

            override fun onInactive() {
                cleanupCalled()
            }
        }

        val subscriber = provider.flowable.subscribe()
        dispatcher.triggerActions()

        subscriber.dispose()
        verifyZeroInteractions(cleanupCalled)

        dispatcher.advanceTime(2000)

        verify(cleanupCalled).invoke()
    }

    @Test
    fun bufferLastItemWhenActive() = runBlocking {
        testProvider.flowable.subscribe()
        dispatcher.triggerActions()

        testProvider.sendPublic(33)
        testProvider.sendPublic(77)

        val lateConsumer: Consumer<Int> = mock()
        testProvider.flowable.subscribe(lateConsumer)
        dispatcher.triggerActions()
        verify(lateConsumer, only()).accept(77)
    }

    @Test
    fun bufferLastItemThroughDebounce() = runBlocking {
        val subscriber = testProvider.flowable.subscribe()
        dispatcher.triggerActions()

        testProvider.sendPublic(33)
        testProvider.sendPublic(77)

        subscriber.dispose()
        dispatcher.advanceTime(200)

        val lateConsumer: Consumer<Int> = mock()
        testProvider.flowable.subscribe(lateConsumer)
        dispatcher.triggerActions()
        verify(lateConsumer, only()).accept(77)
    }

    @Test
    fun bufferLastItemThroughMultipleDebounces() = runBlocking {
        val subscriber = testProvider.flowable.subscribe()
        dispatcher.triggerActions()

        testProvider.sendPublic(33)
        testProvider.sendPublic(77)

        subscriber.dispose()

        testProvider.flowable.subscribe().dispose()
        testProvider.flowable.subscribe().dispose()
        testProvider.flowable.subscribe().dispose()
        testProvider.flowable.subscribe().dispose()

        val lateConsumer: Consumer<Int> = mock()
        testProvider.flowable.subscribe(lateConsumer)
        dispatcher.triggerActions()
        verify(lateConsumer, only()).accept(77)
    }

    @Test
    fun noItemBufferingAfterDispose() = runBlocking {
        val subscriber = testProvider.flowable.subscribe()
        dispatcher.triggerActions()

        testProvider.sendPublic(33)
        testProvider.sendPublic(77)

        subscriber.dispose()
        dispatcher.advanceTime(1000)

        val consumer: Consumer<Int> = mock()
        testProvider.flowable.subscribe(consumer)

        dispatcher.triggerActions()
        verifyZeroInteractions(consumer)
    }

    private fun createTestProvider(debounce: Boolean) {
        onActiveCallback = mock()
        onInactiveCallback = mock()

        testProvider = TestProvider(debounce)

        testProvider.activeCallback = onActiveCallback
        testProvider.inactiveCallback = onInactiveCallback
    }

    private open class TestProvider(debounce: Boolean = true) : OnDemandProvider<Int>(
            debounceTimeout = if (debounce) 500L else 0L
    ) {
        var activeCallback: (() -> Unit)? = null
        var inactiveCallback: (() -> Unit)? = null

        public override suspend fun CoroutineScope.onActive() {
            activeCallback?.invoke()
        }

        public override fun onInactive() {
            inactiveCallback?.invoke()
        }

        suspend fun sendPublic(item: Int) {
            send(item)
        }
    }
}