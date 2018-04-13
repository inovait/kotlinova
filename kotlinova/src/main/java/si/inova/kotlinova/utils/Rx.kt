/**
 * @author Matej Drobnic
 */

@file:JvmName("RxUtils")

package si.inova.kotlinova.utils

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

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