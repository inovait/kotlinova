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