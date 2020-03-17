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

/**
 * @author Matej Drobnic
 */

@file:JvmName("RxUtils")

package si.inova.kotlinova.utils

import io.reactivex.Flowable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactive.publish
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import si.inova.kotlinova.coroutines.TestableDispatchers
import kotlin.coroutines.CoroutineContext

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
// publish is experimental due to undefined behavior when used in structured concurrency.
// We use GlobalScope here so this is not an issue
@UseExperimental(ExperimentalCoroutinesApi::class)
fun <I, O> Flowable<I>.mapAsync(
    context: CoroutineContext = TestableDispatchers.Default,
    mapper: suspend (I) -> O
): Flowable<O> {
    return switchMap { originalValue ->
        publish<O>(context, {
            send(mapper(originalValue))
        })
    }
}