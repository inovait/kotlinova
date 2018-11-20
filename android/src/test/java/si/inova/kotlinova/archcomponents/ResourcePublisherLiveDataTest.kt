package si.inova.kotlinova.archcomponents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.testing.ImmediateDispatcherRule

/**
 * @author Matej Drobnic
 */
class ResourcePublisherLiveDataTest {
    @get:Rule
    val rule = ImmediateDispatcherRule()
    @get:Rule
    val archRule = InstantTaskExecutorRule()

    @Test
    fun propagatingValue() {
        val subscriber = Flowable.just<Resource<Int>>(Resource.Success(10))

        val liveData = ResourcePublisherLiveData(subscriber)
        liveData.observeForever { }

        assertEquals(Resource.Success(10), liveData.value)
    }

    @Test
    fun propagatingErrors() {
        val error = RuntimeException("Test")
        val subscriber = Flowable.error<Resource<Int>>(error)

        val liveData = ResourcePublisherLiveData(subscriber)
        liveData.observeForever { }

        assertEquals(Resource.Error<Int>(error), liveData.value)
    }

    @Test
    fun propagateSubscribeUnsubscribe() {
        val publisher: Publisher<Resource<Int>> = mock(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
        val subscription: Subscription = mock()

        doAnswer {
            val subscriber: Subscriber<Resource<Int>> = it.getArgument(0)
            subscriber.onSubscribe(subscription)
        }.whenever(publisher).subscribe(any())

        inOrder(publisher, subscription) {
            val liveData = ResourcePublisherLiveData(publisher)
            verify(publisher, never()).subscribe(any())

            val observer: Observer<Resource<Int>> = mock()
            liveData.observeForever(observer)
            verify(publisher).subscribe(any())
            verify(subscription, never()).cancel()

            liveData.removeObserver(observer)
            verify(subscription).cancel()
        }
    }
}