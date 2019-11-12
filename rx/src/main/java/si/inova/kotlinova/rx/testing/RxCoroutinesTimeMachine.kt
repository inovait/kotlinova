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