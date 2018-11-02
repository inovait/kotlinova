/**
 * @author Matej Drobnic
 */

@file:JvmName("RxUtils")

package si.inova.kotlinova.utils

import io.reactivex.Flowable
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.reactive.publish
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import si.inova.kotlinova.coroutines.CommonPool
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Subscribe to this publisher, perform certain action and then unsubscribe
 */
inline fun <T> Publisher<T>.use(block: () -> Unit) {
    var subscription: Subscription? = null

    subscribe(object : Subscriber<T> {
        override fun onSubscribe(s: Subscription) {
            subscription = s
        }

        override fun onComplete() = Unit
        override fun onNext(t: T) = Unit
        override fun onError(t: Throwable?) = Unit
    })

    try {
        block()
    } finally {
        subscription?.cancel()
    }
}

/**
 * Map this Flowable to another value with suspending operation.
 * This is similar to [Flowable.map()][Flowable.map], except that mapper function is
 * *suspend* function and supports coroutine operations.
 *
 * Note that if new value arrives from this Flowable and your map operation has not finished
 * processing old value yet, old operation will be cancelled and new one will be started for
 * new value
 */
fun <I, O> Flowable<I>.mapAsync(
    context: CoroutineContext = CommonPool,
    mapper: suspend (I) -> O
): Flowable<O> {
    return switchMap { originalValue ->
        GlobalScope.publish<O>(context, {
            send(mapper(originalValue))
        })
    }
}