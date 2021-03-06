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

import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.reactive.publish
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import si.inova.kotlinova.coroutines.TestableDispatchers
import si.inova.kotlinova.utils.awaitCancellation
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * Data provider that activates when anyone subscribes to its observable and provides a stream
 * of items. After activated, implementors must call [send] method to pass new item into target
 * observable.
 *
 * Items can be consumed by subscribing to [flowable] property.
 *
 * Class also automatically handles debouncing (when there are no subscribers left, provider
 * will stay active for a short time in case subscribers arrive back). Implementors can control
 * this behavior using [debounceTimeout] parameter.
 *
 * @author Matej Drobnic
 */

abstract class OnDemandProvider<T>(
    private val launchingContext: CoroutineContext = TestableDispatchers.Default,
    rxScheduler: Scheduler = Schedulers.computation(),
    val debounceTimeout: Long = DEFAULT_DEBOUNCE_TIMEOUT
) {
    private val mutext = Mutex()

    @Volatile
    var isActive: Boolean = false
        private set

    @Suppress("EXPERIMENTAL_API_USAGE")
    private var producer: ProducerScope<T>? = null

    /**
     * Method that gets triggered when someone starts observing your service
     */
    protected abstract suspend fun CoroutineScope.onActive()

    /**
     * Flowable that relays stream of items transmitted from that provider
     */
    // publish is experimental due to undefined behavior when used in structured concurrency.
    // We use GlobalScope here so this is not an issue
    @Suppress("EXPERIMENTAL_API_USAGE")
    val flowable: Flowable<T> = Flowable.fromPublisher(publish<T>(launchingContext) {
        mutext.withLock {
            try {
                producer = this
                isActive = true
                onActive()
                awaitCancellation()
            } finally {
                isActive = false

                onInactive()
                producer = null
            }
        }
    })
        .replay(1)
        .run {
            if (debounceTimeout <= 0) {
                refCount()
            } else {
                refCount(debounceTimeout.toLong(), TimeUnit.MILLISECONDS)
            }
        }
        .subscribeOn(rxScheduler)

    protected suspend fun send(value: T) {
        val producer =
            producer ?: throw IllegalStateException("Cannot send data when provider is inactive")

        @Suppress("EXPERIMENTAL_API_USAGE")
        producer.send(value)
    }

    protected fun sendBlocking(value: T) {
        val producer =
            producer ?: throw IllegalStateException("Cannot send data when provider is inactive")

        @Suppress("EXPERIMENTAL_API_USAGE")
        producer.sendBlocking(value)
    }

    protected fun offer(value: T) {
        val producer =
            producer ?: throw IllegalStateException("Cannot send data when provider is inactive")

        @Suppress("EXPERIMENTAL_API_USAGE")
        producer.offer(value)
    }

    /**
     * Method that gets triggered when everyone stops observing your service
     */
    protected open fun onInactive() = Unit
}

private const val DEFAULT_DEBOUNCE_TIMEOUT = 500L