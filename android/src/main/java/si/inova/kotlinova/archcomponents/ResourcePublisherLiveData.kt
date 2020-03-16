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

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import si.inova.kotlinova.data.resources.Resource
import java.util.concurrent.atomic.AtomicReference

/**
 * LiveData that wraps around provided [Publisher] and returns data stream as LiveData.
 *
 * This class is similar to
 * [LiveDataReactiveStreams.fromPublisher()][LiveDataReactiveStreams.fromPublisher], except that it
 * only supports [Resource] streams and returns all errors as [Resource.Error] instead of throwing.
 *
 * @author original PublisherLiveData by Google, adapted by Matej Drobnic
 */
class ResourcePublisherLiveData<T>(private val publisher: Publisher<Resource<T>>) :
        LiveData<Resource<T>>() {
    internal val mSubscriber: AtomicReference<LiveDataSubscriber> = AtomicReference()

    override fun onActive() {
        super.onActive()
        val s = LiveDataSubscriber()
        mSubscriber.set(s)
        publisher.subscribe(s)
    }

    override fun onInactive() {
        super.onInactive()
        val s = mSubscriber.getAndSet(null)
        s?.cancelSubscription()
    }

    internal inner class LiveDataSubscriber : AtomicReference<Subscription>(),
            Subscriber<Resource<T>> {

        override fun onSubscribe(s: Subscription) {
            if (compareAndSet(null, s)) {
                s.request(java.lang.Long.MAX_VALUE)
            } else {
                s.cancel()
            }
        }

        override fun onNext(item: Resource<T>) {
            postValue(item)
        }

        override fun onError(ex: Throwable) {
            mSubscriber.compareAndSet(this, null)
            postValue(Resource.Error(ex))
        }

        override fun onComplete() {
            mSubscriber.compareAndSet(this, null)
        }

        fun cancelSubscription() {
            val s = get()
            s?.cancel()
        }
    }
}