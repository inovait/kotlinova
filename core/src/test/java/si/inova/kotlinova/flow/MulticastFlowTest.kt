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

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeoutOrNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class MulticastFlowTest {
    @get:Rule
    val timeout = Timeout(5, TimeUnit.SECONDS)

    @get:Rule
    val exceptionsRule = UncaughtExceptionThrowRule()

    @Test
    fun `only launch flow once`() = runBlocking<Unit> {
        val numLaunches = AtomicInteger(0)

        val flow = flow {
            numLaunches.incrementAndGet()
            emit("A")

            delay(5)
        }.share()

        val tasks = ArrayList<Job>()

        tasks += launch {
            flow.collect {}
        }

        tasks += launch {
            flow.collect {}
        }

        tasks.joinAll()

        assertThat(numLaunches.get()).isEqualTo(1)
    }

    @Test
    fun `receive items on all collectors`() = runBlocking<Unit> {
        val itemsA = ArrayList<String>()
        val itemsB = ArrayList<String>()

        val flow = flow {
            delay(5)

            emit("A")
            emit("B")
            emit("C")
        }.share()

        val tasks = ArrayList<Job>()

        tasks += launch {
            flow.collect {
                itemsA += it
            }
        }

        tasks += launch {
            flow.collect {
                itemsB += it
            }
        }

        tasks.joinAll()

        assertThat(itemsA).containsExactly("A", "B", "C")
        assertThat(itemsB).containsExactly("A", "B", "C")
    }

    @Test
    fun `close flow after all collectors close`() = runBlocking<Unit> {
        val closedCompletable = CompletableDeferred<Unit>()

        val flow = flow {
            try {
                delay(10)
                emit("A")
                suspendCancellableCoroutine<Unit> { }
            } finally {
                closedCompletable.complete(Unit)
            }
        }.share()

        val tasks = ArrayList<Job>()

        tasks += launch {
            flow.collect {
                coroutineContext.cancel()
            }
        }

        tasks += launch {
            flow.collect {
                coroutineContext.cancel()
            }
        }

        tasks.joinAll()

        withTimeoutOrNull(5_000) { closedCompletable.join() } ?: error("Flow not closed")
    }

    @Test
    fun `do not crash the whole flow if one collector throws exception`() = runBlocking<Unit> {
        val receivedItems = ArrayList<String>()

        val flow = flow {
            delay(5)

            emit("A")
            emit("B")
            emit("C")
        }.share()

        val tasks = ArrayList<Job>()

        tasks += launch(Dispatchers.Unconfined) {
            flow.collect {
                delay(10)
                receivedItems += it
            }
        }

        tasks += launch(Dispatchers.Unconfined) {
            try {
                flow.collect {
                    throw IllegalStateException("Test")
                }
            } catch (e: Exception) {
            }
        }

        tasks.joinAll()

        assertThat(receivedItems).contains("A", "B", "C")
    }

    @Test
    fun `receive exceptions on all producers`() = runBlocking<Unit> {
        val receivedA = AtomicBoolean(false)
        val receivedB = AtomicBoolean(false)

        val flow = flow<String> {
            delay(5)

            throw CloneNotSupportedException()
        }.share()

        val tasks = ArrayList<Job>()

        tasks += launch {
            try {
                flow.collect {}
            } catch (e: CloneNotSupportedException) {
                receivedA.set(true)
            }
        }

        tasks += launch {
            try {
                flow.collect {}
            } catch (e: CloneNotSupportedException) {
                receivedB.set(true)
            }
        }

        tasks.joinAll()

        assertThat(receivedA.get()).isTrue()
        assertThat(receivedB.get()).isTrue()
    }

    @Test
    fun `receive last item when new collector starts collecting existing flow`() =
        runBlocking<Unit> {
            val itemsA = ArrayList<String>()
            val itemsB = ArrayList<String>()

            val flow = flow {
                delay(5)

                emit("A")
                emit("B")
                emit("C")

                delay(15)
            }.share(conflate = true)

            val tasks = ArrayList<Job>()

            tasks += launch {
                flow.collect {
                    itemsA += it
                }
            }

            tasks += launch {
                delay(10)
                flow.collect {
                    itemsB += it
                }
            }

            tasks.joinAll()

            assertThat(itemsA).containsExactly("A", "B", "C")
            assertThat(itemsB).containsExactly("C")
        }

    @Test
    fun `do not receive conflated last item when there were no active collectors`() =
        runBlocking<Unit> {
            val itemsA = ArrayList<String>()
            val itemsB = ArrayList<String>()

            val flow = flow {
                delay(5)

                emit("A")
                emit("B")
                emit("C")

                delay(15)
            }.share(conflate = true)

            flow.collect {
                itemsA += it
            }

            flow.collect {
                itemsB += it
            }

            assertThat(itemsA).containsExactly("A", "B", "C")
            assertThat(itemsB).containsExactly("A", "B", "C")
        }

    @Test
    fun `do not receive last item when conflate is disabled`() = runBlocking<Unit> {
        val itemsA = ArrayList<String>()
        val itemsB = ArrayList<String>()

        val semaphore = Semaphore(1)
        semaphore.acquire()

        val flow = flow {
            delay(5)

            emit("A")
            emit("B")
            emit("C")

            delay(15)

            semaphore.release()

            delay(15)
        }.share(conflate = false)

        val tasks = ArrayList<Job>()

        tasks += launch {
            flow.collect {
                itemsA += it
            }
        }

        tasks += launch {
            semaphore.acquire()
            flow.collect {
                itemsB += it
            }
        }

        tasks.joinAll()

        assertThat(itemsA).containsExactly("A", "B", "C")
        assertThat(itemsB).isEmpty()
    }

    @Test
    fun `do not close flow within debounce period`() = runBlocking<Unit> {
        val numLaunches = AtomicInteger(0)

        val flow = flow {
            numLaunches.incrementAndGet()

            var counter = 0
            while (true) {
                emit(counter++)
                delay(5)
            }
        }.share(debounceMs = 500)

        val tasks = ArrayList<Job>()

        tasks += launch {
            withTimeoutOrNull(10) {
                flow.collect {}
            }
        }

        delay(20)

        tasks += launch {
            withTimeoutOrNull(10) {
                flow.collect {}
            }
        }

        tasks.joinAll()

        assertThat(numLaunches.get()).isEqualTo(1)
    }

    @Test
    fun `close flow after debounce period`() = runBlocking<Unit> {
        val numLaunches = AtomicInteger(0)

        val flow = flow {
            numLaunches.incrementAndGet()
            emit("A")

            delay(999)
        }.share(debounceMs = 20)

        val tasks = ArrayList<Job>()

        tasks += launch {
            withTimeoutOrNull(10) {
                flow.collect {}
            }
        }

        delay(100)

        tasks += launch {
            withTimeoutOrNull(10) {
                flow.collect {}
            }
        }

        tasks.joinAll()
        assertThat(numLaunches.get()).isEqualTo(2)
    }

    @Test
    fun `cancel debounce when client rejoins`() = runBlocking<Unit> {
        val numLaunches = AtomicInteger(0)

        val flow = flow {
            numLaunches.incrementAndGet()
            emit("A")

            delay(999)
        }.share(debounceMs = 30)

        val tasks = ArrayList<Job>()

        tasks += launch {
            withTimeoutOrNull(5) {
                flow.collect {}
            }
        }

        delay(5)

        tasks += launch {
            withTimeoutOrNull(50) {
                flow.collect {}
            }
        }

        tasks.joinAll()
        assertThat(numLaunches.get()).isEqualTo(1)
    }

    @Test
    fun `launch a lot of collectors in short span of time on same thread`() = runBlocking<Unit> {
        repeat(100) {
            val flow = flow {
                emit(1)
            }.share(true, 0)

            val sum = AtomicInteger(0)
            repeat(100) {
                flow.collect {
                    sum.getAndAdd(it)
                }
            }

            assertThat(sum.get()).isEqualTo(100)
        }
    }

    @Test
    fun `collect every value at least once when collecting from multiple threads`() =
        runBlocking<Unit> {
            repeat(100) {
                val flow = flow {
                    emit(1)
                }.share(true, 0)

                val sum = AtomicInteger(0)
                val flows = List(100) {
                    launch {
                        var alreadyCollected = false
                        flow.collect {
                            if (!alreadyCollected) {
                                sum.getAndAdd(it)
                                alreadyCollected = true
                            }
                        }
                    }
                }

                flows.joinAll()

                assertThat(sum.get()).isEqualTo(100)
            }
        }

    @Test
    fun `collect every value at least once when collecting from multiple threads with debounce`() =
        runBlocking<Unit> {
            repeat(20) {
                val flow = flow {
                    emit(1)
                    delay(50)
                }.share(true, 100)

                val sum = AtomicInteger(0)
                val flows = List(100) {
                    launch {
                        var alreadyCollected = false
                        flow.collect {
                            if (!alreadyCollected) {
                                sum.getAndAdd(it)
                                alreadyCollected = true
                            }
                        }
                    }
                }

                flows.joinAll()

                assertThat(sum.get()).isEqualTo(100)
            }
        }

    @Test
    fun `do not leak anything to GlobalScope`() = runBlocking<Unit> {
        val flow = createWeakFlow()

        System.gc()
        delay(100)
        System.gc()

        assertThat(flow.get()).isNull()
    }

    private suspend fun createWeakFlow(): WeakReference<Flow<String>> {
        val flow = flow {
            emit("A")

            delay(999)
        }.share(debounceMs = 50)

        flow.collect {}

        return WeakReference(
            flow
        )
    }
}
