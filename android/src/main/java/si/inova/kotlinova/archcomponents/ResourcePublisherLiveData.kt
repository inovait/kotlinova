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