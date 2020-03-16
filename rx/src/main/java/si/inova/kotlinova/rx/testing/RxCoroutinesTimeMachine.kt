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

package si.inova.kotlinova.rx.testing

import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.TestableDispatchers
import si.inova.kotlinova.testing.TimeMachine
import si.inova.kotlinova.time.JavaTimeProvider
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * JUnit Rule that installs and controls [TestCoroutineDispatcher], [TestScheduler] and
 * [JavaTimeProvider] at once.
 */
@Suppress("EXPERIMENTAL_API_USAGE")
@UseExperimental(ObsoleteCoroutinesApi::class)
class RxCoroutinesTimeMachine : TestWatcher(), TimeMachine {
    val rxScheduler = TestScheduler()
    val coroutineContext = TestCoroutineDispatcher()

    override fun starting(description: Description?) {
        super.starting(description)

        coroutineContext.pauseDispatcher()

        TestableDispatchers.dispatcherOverride = { coroutineContext }
        Dispatchers.setMain(coroutineContext)
        JavaTimeProvider.currentTimeMillisProvider = { now }
        JavaTimeProvider.clockProvider =
                { Clock.fixed(Instant.ofEpochMilli(now), ZoneId.of("UTC")) }

        RxJavaPlugins.reset()

        val schedulerFunction: (Scheduler) -> Scheduler = { rxScheduler }
        RxJavaPlugins.setIoSchedulerHandler(schedulerFunction)
        RxJavaPlugins.setNewThreadSchedulerHandler(schedulerFunction)
        RxJavaPlugins.setComputationSchedulerHandler(schedulerFunction)
    }

    override fun finished(description: Description?) {
        super.finished(description)

        RxJavaPlugins.reset()

        TestableDispatchers.dispatcherOverride = { it() }
        JavaTimeProvider.currentTimeMillisProvider = { System.currentTimeMillis() }
        JavaTimeProvider.clockProvider = { Clock.systemUTC() }
    }

    /**
     * Moves the Scheduler's clock forward by a specified amount of milliseconds.
     */
    override fun advanceTime(ms: Long) {
        rxScheduler.advanceTimeBy(ms, TimeUnit.MILLISECONDS)
        coroutineContext.advanceTimeBy(ms)
    }

    /**
     * Triggers any actions that have not yet been triggered and that are scheduled to be triggered at or
     * before this Scheduler's present time.
     */
    fun triggerActions() {
        rxScheduler.triggerActions()
        coroutineContext.runCurrent()
    }

    override val now: Long
        get() = rxScheduler.now(TimeUnit.MILLISECONDS)
}