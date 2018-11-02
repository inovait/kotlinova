package si.inova.kotlinova.utils

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.subscribers.TestSubscriber
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.reactivestreams.Subscription
import si.inova.kotlinova.testing.RxSchedulerRule
import si.inova.kotlinova.testing.TimedDispatcher
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule

/**
 * @author Matej Drobnic
 */
class RxUtilsTest {
    @get:Rule
    val dispatcher = TimedDispatcher()
    @get:Rule
    val rxRule = RxSchedulerRule()
    @get:Rule
    val exceptionsRule = UncaughtExceptionThrowRule()

    @Test
    fun use() {
        val onSubscribe: Consumer<in Subscription> = mock()
        val onDispose: Action = mock()
        val action: () -> Unit = mock()

        val flowable = Flowable.never<Unit>()
            .doOnSubscribe(onSubscribe)
            .doFinally(onDispose)

        inOrder(onSubscribe, action, onDispose) {
            verifyNoMoreInteractions()

            flowable.use(action)

            verify(onSubscribe).accept(any())
            verify(action).invoke()
            verify(onDispose).run()

            verifyNoMoreInteractions()
        }
    }

    @Test
    fun useWithException() {
        val onSubscribe: Consumer<in Subscription> = mock()
        val onDispose: Action = mock()
        val action: () -> Unit = mock()

        whenever(action.invoke()).thenThrow(RuntimeException())

        val flowable = Flowable.never<Unit>()
            .doOnSubscribe(onSubscribe)
            .doFinally(onDispose)

        inOrder(onSubscribe, action, onDispose) {
            verifyNoMoreInteractions()

            try {
                flowable.use(action)
            } catch (e: Exception) {
            }

            verify(onSubscribe).accept(any())
            verify(action).invoke()
            verify(onDispose).run()

            verifyNoMoreInteractions()
        }
    }

    @Test
    fun mapAsync() {
        val original = Flowable.just(5, 9, 12, 30)

        val mapped = original.mapAsync { it * 100 }

        val testSubscriber = TestSubscriber<Int>()
        mapped.subscribe(testSubscriber)

        testSubscriber.assertValues(500, 900, 1200, 3000)
        testSubscriber.assertComplete()
        testSubscriber.assertNoErrors()
    }

    @Test
    fun mapAsyncDropOldValues() {
        val original = Flowable.just(5, 9, 12, 30)

        val mapped = original.mapAsync {
            delay(1000)
            it * 100
        }

        val testSubscriber = TestSubscriber<Int>()
        mapped.subscribe(testSubscriber)

        dispatcher.advanceTime(100000)

        testSubscriber.assertValues(3000)
        testSubscriber.assertComplete()
        testSubscriber.assertNoErrors()
    }
}