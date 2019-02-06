package si.inova.kotlinova.rx.testing

import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineContext
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.MultipleFailureException
import si.inova.kotlinova.coroutines.TestableDispatchers
import si.inova.kotlinova.testing.TimeMachine
import si.inova.kotlinova.time.JavaTimeProvider
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * JUnit Rule that installs and controls [TestCoroutineContext], [TestScheduler] and
 * [JavaTimeProvider] at once.
 */
@UseExperimental(ObsoleteCoroutinesApi::class)
class RxCoroutinesTimeMachine : TestWatcher(), TimeMachine {
    val rxScheduler = TestScheduler()
    val coroutineContext = TestCoroutineContext()

    override fun starting(description: Description?) {
        super.starting(description)

        TestableDispatchers.dispatcherOverride = { coroutineContext }
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

        MultipleFailureException.assertEmpty(coroutineContext.exceptions)
    }

    /**
     * Moves the Scheduler's clock forward by a specified amount of milliseconds.
     */
    override fun advanceTime(ms: Long) {
        rxScheduler.advanceTimeBy(ms, TimeUnit.MILLISECONDS)
        coroutineContext.advanceTimeBy(ms, TimeUnit.MILLISECONDS)
    }

    /**
     * Triggers any actions that have not yet been triggered and that are scheduled to be triggered at or
     * before this Scheduler's present time.
     */
    fun triggerActions() {
        rxScheduler.triggerActions()
        coroutineContext.triggerActions()
    }

    override val now: Long
        get() = rxScheduler.now(TimeUnit.MILLISECONDS)
}