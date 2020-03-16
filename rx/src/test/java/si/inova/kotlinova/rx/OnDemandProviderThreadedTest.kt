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

import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.TimberExceptionThrowRule
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Matej Drobnic
 */
class OnDemandProviderThreadedTest {
    @get:Rule
    val exceptionRule = UncaughtExceptionThrowRule()

    @get:Rule
    val timberRule = TimberExceptionThrowRule()

    @Test(timeout = 10_000)
    fun manySubscribes() {
        // Thread concurrency issues do not reproduce every time
        // repeat it multiple times to increase chance of failure
        repeat(100) {
            val provider = TestProvider(debounce = false)

            repeat(200) {
                provider.flowable.subscribe().dispose()
            }
        }

        ForkJoinPool.commonPool().awaitTermination(5, TimeUnit.DAYS)
    }

    @Test(timeout = 10_000)
    fun manySubscribesWithDebounce() {
        // Thread concurrency issues do not reproduce every time
        // repeat it multiple times to increase chance of failure
        repeat(100) {
            val provider = TestProvider()

            repeat(200) {
                provider.flowable.subscribe().dispose()
            }
        }

        ForkJoinPool.commonPool().awaitTermination(5, TimeUnit.DAYS)
    }

    private open class TestProvider(debounce: Boolean = true) : OnDemandProvider<Unit>(
            rxScheduler = Schedulers.trampoline(),
            debounceTimeout = if (debounce) 500L else 0L
    ) {
        private val currentlyActive = AtomicBoolean(false)

        override suspend fun CoroutineScope.onActive() {
            assertFalse(currentlyActive.getAndSet(true))
            assertTrue(this@TestProvider.isActive)
            assertTrue(isActive)

            delay(500)
        }

        override fun onInactive() {
            assertTrue(currentlyActive.getAndSet(false))
            assertFalse(this@TestProvider.isActive)
        }
    }
}