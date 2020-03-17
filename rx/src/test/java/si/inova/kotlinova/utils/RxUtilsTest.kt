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