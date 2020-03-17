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

import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BehaviorSubjectProxyTest {
    private lateinit var testObservable: PublishSubject<Int>
    private lateinit var behaviorSubjectProxy: BehaviorSubjectProxy<Int>

    @Before
    fun setUp() {
        testObservable = PublishSubject.create<Int>()
        behaviorSubjectProxy =
            BehaviorSubjectProxy(testObservable.toFlowable(BackpressureStrategy.LATEST))
    }

    @Test
    fun itemFlow() {
        val subscriber = TestSubscriber<Int>()
        behaviorSubjectProxy.flowable.subscribe(subscriber)

        testObservable.onNext(1)
        testObservable.onNext(2)
        testObservable.onNext(3)

        subscriber.assertValueSequence(1..3)
    }

    @Test
    fun subscribeUnsubscribe() {
        assertFalse(testObservable.hasObservers())

        val subscriber = TestSubscriber<Int>()
        behaviorSubjectProxy.flowable.subscribe(subscriber)

        assertTrue(testObservable.hasObservers())

        subscriber.dispose()
        assertFalse(testObservable.hasObservers())
    }

    @Test
    fun subscribeUnsubscribeMultipleSubscribers() {
        assertFalse(testObservable.hasObservers())

        val subscriberA = TestSubscriber<Int>()
        behaviorSubjectProxy.flowable.subscribe(subscriberA)
        val subscriberB = TestSubscriber<Int>()
        behaviorSubjectProxy.flowable.subscribe(subscriberB)

        assertTrue(testObservable.hasObservers())

        subscriberA.dispose()
        assertTrue(testObservable.hasObservers())
        subscriberB.dispose()
        assertFalse(testObservable.hasObservers())
    }
}