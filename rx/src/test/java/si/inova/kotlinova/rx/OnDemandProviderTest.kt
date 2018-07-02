package si.inova.kotlinova.rx

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.only
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.functions.Consumer
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.NonCancellable
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.RxSchedulerRule
import si.inova.kotlinova.testing.TimedDispatcher
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import timber.log.Timber

/**
 * @author Matej Drobnic
 */
class OnDemandProviderTest {
    private lateinit var testProvider: TestProvider
    private lateinit var onActiveCallback: () -> Unit
    private lateinit var onInactiveCallback: () -> Unit

    @get:Rule
    val dispatcher = TimedDispatcher()
    @get:Rule
    val rxJavaSchedulerRule = RxSchedulerRule()
    @get:Rule
    val uncaughtExceptionThrowRule = UncaughtExceptionThrowRule()

    @Before
    fun setUp() {
        testProvider = TestProvider()

        onActiveCallback = mock()
        onInactiveCallback = mock()

        testProvider.activeCallback = onActiveCallback
        testProvider.inactiveCallback = onInactiveCallback
    }

    @Test
    fun ActiveInactive() {
        // Debounce is not yet tested here
        testProvider.disableDebounce()

        inOrder(onActiveCallback, onInactiveCallback) {
            runBlocking {
                verifyNoMoreInteractions()

                val subscriber = testProvider.flowable.subscribe()
                verify(onActiveCallback).invoke()

                subscriber.dispose()
                verify(onInactiveCallback).invoke()

                verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun ActiveInactiveMultipleSubscribers() {
        // Debounce is not yet tested here
        testProvider.disableDebounce()

        inOrder(onActiveCallback, onInactiveCallback) {
            runBlocking {
                verifyNoMoreInteractions()

                val subscriberA = testProvider.flowable.subscribe()
                val subscriberB = testProvider.flowable.subscribe()
                verify(onActiveCallback).invoke()

                subscriberA.dispose()
                verifyNoMoreInteractions()
                subscriberB.dispose()

                verify(onInactiveCallback).invoke()

                verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun isActive() {
        // Debounce is not yet tested here
        testProvider.disableDebounce()

        assertFalse(testProvider.isActive)

        val subscriberA = testProvider.flowable.subscribe()
        val subscriberB = testProvider.flowable.subscribe()
        assertTrue(testProvider.isActive)

        subscriberA.dispose()
        assertTrue(testProvider.isActive)

        subscriberB.dispose()
        assertFalse(testProvider.isActive)
    }

    @Test
    fun receiveData() {
        runBlocking {
            val consumer: Consumer<Int> = mock()
            val subscriber = testProvider.flowable.subscribe(consumer)

            inOrder(consumer) {
                testProvider.sendPublic(1)
                verify(consumer).accept(1)
                testProvider.sendPublic(20)
                verify(consumer).accept(20)
                testProvider.sendPublic(33)
                verify(consumer).accept(33)
                testProvider.sendPublic(45)
                verify(consumer).accept(45)

                subscriber.dispose()

                verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun receiveCrashExceptions() {
        // Debounce is not yet tested here
        testProvider.disableDebounce()

        runBlocking {
            val exception = RuntimeException("Test")
            val consumer: Consumer<Int> = mock()
            val errorConsumer: Consumer<in Throwable> = mock()
            whenever(onActiveCallback.invoke()).thenThrow(exception)

            val subscriber = testProvider.flowable.subscribe(consumer, errorConsumer)

            verify(errorConsumer).accept(exception)
            verify(onInactiveCallback).invoke()
            assertTrue(subscriber.isDisposed)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun crashIfEmittingBeforeDispose() {
        testProvider.sendPublic(10)
    }

    @Test(expected = IllegalStateException::class)
    fun crashIfEmittingAfterDispose() {
        // Debounce is not yet tested here
        testProvider.disableDebounce()

        val subscriber = testProvider.flowable.subscribe()
        testProvider.sendPublic(20)
        subscriber.dispose()

        testProvider.sendPublic(10)
    }

    @Test
    fun cancelOnActiveAfterDispose() {
        val afterDelay: () -> Unit = mock()

        val provider = object : OnDemandProvider<Int>() {
            init {
                // Debounce is not yet tested here
                debounceTimeout = 0
            }

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
                verify(onActiveCallback).invoke()

                subscriber.dispose()
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
        assertTrue(testProvider.isActive)

        subscriber.dispose()
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
                verifyNoMoreInteractions()

                var subscriber = testProvider.flowable.subscribe()
                verify(onActiveCallback).invoke()

                subscriber.dispose()
                verifyNoMoreInteractions()

                dispatcher.advanceTime(200)
                verifyNoMoreInteractions()

                subscriber = testProvider.flowable.subscribe()

                dispatcher.advanceTime(2000)

                verifyNoMoreInteractions()

                subscriber.dispose()
                dispatcher.advanceTime(200)

                verifyNoMoreInteractions()

                dispatcher.advanceTime(2000)

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
                verify(onActiveCallback).invoke()

                subscriber.dispose()

                dispatcher.advanceTime(200)

                testProvider.flowable.subscribe()

                verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun receiveValueTransmittedWhenInDebounce() {
        val consumer: Consumer<Int> = mock()

        runBlocking {
            val subscriber = testProvider.flowable.subscribe()
            subscriber.dispose()

            dispatcher.advanceTime(100)
            testProvider.sendPublic(15)
            dispatcher.advanceTime(100)

            testProvider.flowable.subscribe(consumer)
            verify(consumer).accept(15)
        }
    }

    @Test
    fun receiveValueTransmittedAfterDebounce() {
        val consumer: Consumer<Int> = mock()

        runBlocking {
            val subscriber = testProvider.flowable.subscribe()
            subscriber.dispose()

            dispatcher.advanceTime(100)

            testProvider.flowable.subscribe(consumer)

            testProvider.sendPublic(15)
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
    fun printExceptionOnSendWhenInDebounce() {
        // Coroutines have issues with propagating exceptions after cancellation
        // Log with timber instead
        val timberTree: Timber.Tree = mock()
        Timber.plant(timberTree)

        val exception = IllegalStateException()

        val provider = object : OnDemandProvider<Int>() {
            override suspend fun CoroutineScope.onActive() {
                withContext(NonCancellable) {
                    delay(1000)
                }

                throw exception
            }
        }

        provider.flowable.subscribe().dispose()
        dispatcher.advanceTime(2000)
        verify(timberTree).e(exception)

        Timber.uproot(timberTree)
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
            verify(onActiveStart).invoke()

            subscriber.dispose()
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

        subscriber.dispose()
        verifyZeroInteractions(cleanupCalled)

        dispatcher.advanceTime(2000)

        verify(cleanupCalled).invoke()
    }

    @Test
    fun doNotCancelCleanup() {
        val cleanupFinished: () -> Unit = mock()

        val provider = object : OnDemandProvider<Unit>() {
            override suspend fun CoroutineScope.onActive() {
                delay(99999999)
            }

            override fun onInactive() {
                cleanupFinished()
            }
        }

        val subscriber = provider.flowable.subscribe()

        subscriber.dispose()

        dispatcher.advanceTime(2000)

        verify(cleanupFinished).invoke()
    }

    @Test
    fun bufferLastItemWhenActive() {
        testProvider.flowable.subscribe()
        testProvider.sendPublic(33)
        testProvider.sendPublic(77)

        val lateConsumer: Consumer<Int> = mock()
        testProvider.flowable.subscribe(lateConsumer)
        verify(lateConsumer, only()).accept(77)
    }

    @Test
    fun bufferLastItemThroughDebounce() {
        val subscriber = testProvider.flowable.subscribe()
        testProvider.sendPublic(33)
        testProvider.sendPublic(77)

        subscriber.dispose()
        dispatcher.advanceTime(200)

        val lateConsumer: Consumer<Int> = mock()
        testProvider.flowable.subscribe(lateConsumer)
        verify(lateConsumer, only()).accept(77)
    }

    @Test
    fun bufferLastItemThroughMultipleDebounces() {
        val subscriber = testProvider.flowable.subscribe()
        testProvider.sendPublic(33)
        testProvider.sendPublic(77)

        subscriber.dispose()

        testProvider.flowable.subscribe().dispose()
        testProvider.flowable.subscribe().dispose()
        testProvider.flowable.subscribe().dispose()
        testProvider.flowable.subscribe().dispose()

        val lateConsumer: Consumer<Int> = mock()
        testProvider.flowable.subscribe(lateConsumer)
        verify(lateConsumer, only()).accept(77)
    }

    @Test
    fun noItemBufferingAfterDispose() {
        val subscriber = testProvider.flowable.subscribe()
        testProvider.sendPublic(33)
        testProvider.sendPublic(77)

        subscriber.dispose()
        dispatcher.advanceTime(1000)

        val consumer: Consumer<Int> = mock()
        testProvider.flowable.subscribe(consumer)

        verifyZeroInteractions(consumer)
    }

    private open class TestProvider : OnDemandProvider<Int>() {
        var activeCallback: (() -> Unit)? = null
        var inactiveCallback: (() -> Unit)? = null

        public override suspend fun CoroutineScope.onActive() {
            runBlocking {
                activeCallback?.invoke()
            }
        }

        public override fun onInactive() {
            inactiveCallback?.invoke()
        }

        fun sendPublic(item: Int) {
            this.send(item)
        }

        fun disableDebounce() {
            debounceTimeout = 0
        }
    }
}