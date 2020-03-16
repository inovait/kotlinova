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

package si.inova.kotlinova.archcomponents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.*
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