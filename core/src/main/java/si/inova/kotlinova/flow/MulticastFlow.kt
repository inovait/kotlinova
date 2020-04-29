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

package si.inova.kotlinova.flow

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import si.inova.kotlinova.utils.containsOnly
import si.inova.kotlinova.utils.update
import java.util.concurrent.atomic.AtomicReference

@Suppress("EXPERIMENTAL_API_USAGE")
internal class MulticastFlow<T>(
    private val original: Flow<T>,
    private val conflate: Boolean,
    private val debounceMs: Int
) {
    private val mutex = Mutex()

    private val state: AtomicReference<MulticastState<T>> =
        AtomicReference(MulticastState<T>(false, null, emptyList()))

    @Volatile
    private var broadcastingFlowCollectorJob: Job? = null

    @Volatile
    private var debounceJob: Job? = null

    private suspend fun addCollector(flow: FlowCollector<T>) = mutex.withLock {
        val channel = Channel<T>()

        val hasNoCollectorsBeforeAdding: Boolean = state.get().collectors.isEmpty()
        state.update {
            if (it.hasLastValue) {
                flow.emit(it.lastValue!!)
            }

            it.copy(collectors = it.collectors + channel)
        }

        if (broadcastingFlowCollectorJob?.isActive != true ||
            (hasNoCollectorsBeforeAdding && debounceJob?.isActive != true)) {
            startBroadcastingFlowCollector()
        }

        debounceJob?.cancel()
        channel
    }

    private suspend fun removeCollector(channel: SendChannel<T>) = mutex.withLock {
        val collectors = state.get().collectors
        if (collectors.isEmpty() || collectors.containsOnly(channel)) {
            if (debounceMs <= 0) {
                stopBroadcastingFlowCollector()
            } else {
                state.update {
                    it.copy(collectors = it.collectors - channel)
                }

                startDebounceJob()
            }
        } else {
            state.update {
                it.copy(collectors = it.collectors - channel)
            }
        }
    }

    private fun startBroadcastingFlowCollector() {
        broadcastingFlowCollectorJob = GlobalScope.launch {
            try {
                original.collect { value ->
                    state.update {
                        it.collectors.forEach { collector ->
                            try {
                                collector.send(value)
                            } catch (e: Exception) {
                            }
                        }

                        val lastValue: T?
                        val hasLastValue: Boolean

                        if (conflate) {
                            lastValue = value
                            hasLastValue = true
                        } else {
                            lastValue = null
                            hasLastValue = false
                        }

                        it.copy(
                            hasLastValue = hasLastValue,
                            lastValue = lastValue
                        )
                    }
                }

                state.update {
                    it.collectors.forEach { collector ->
                        collector.close()
                    }

                    it.copy(collectors = emptyList())
                }
            } catch (e: Exception) {
                state.update {
                    it.collectors.forEach { collector ->
                        collector.close(e)
                    }

                    it.copy(collectors = emptyList())
                }
            }
        }
    }

    private fun startDebounceJob() {
        debounceJob = GlobalScope.launch {
            delay(debounceMs.toLong())

            mutex.withLock {
                if (state.get().collectors.isNotEmpty()) {
                    throw IllegalStateException(
                        "Collectors should be empty when stopping broadcasts"
                    )
                }

                stopBroadcastingFlowCollector()
            }
        }
    }

    private suspend fun stopBroadcastingFlowCollector() {
        broadcastingFlowCollectorJob?.cancelAndJoin()

        state.update {
            it.copy(lastValue = null, hasLastValue = false)
        }
    }

    val multicastedFlow = flow {
        var channel: Channel<T>? = null
        try {
            channel = addCollector(this)
            emitAll(channel.consumeAsFlow())
        } finally {
            channel?.let { removeCollector(it) }
        }
    }
}

private data class MulticastState<T>(
    val hasLastValue: Boolean,
    val lastValue: T?,
    val collectors: List<SendChannel<T>>
)

/**
 * Allow multiple collectors to collect same instance of this flow. This flow will be opened when
 * first collector starts collecting and closed after last collector gets cancelled.
 *
 * Collectors of this flows are guaranteed to receive every upstream value at least once.
 *
 * @param conflate Whether new collector should receive last collected value
 * @param debounceMs Number of milliseconds to wait after last collector closes
 * before closing original flow. Set to 0 to disable.
 */
fun <T> Flow<T>.share(
    conflate: Boolean = false,
    debounceMs: Int = 0
): Flow<T> {
    return MulticastFlow(this, conflate, debounceMs).multicastedFlow
}
