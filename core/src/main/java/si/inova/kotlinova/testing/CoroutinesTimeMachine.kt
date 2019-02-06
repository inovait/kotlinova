package si.inova.kotlinova.testing

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineContext
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.MultipleFailureException
import si.inova.kotlinova.coroutines.TestableDispatchers
import si.inova.kotlinova.time.JavaTimeProvider
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * JUnit Rule that installs and controls [TestCoroutineContext] and
 * [JavaTimeProvider] at once.
 */
@UseExperimental(ObsoleteCoroutinesApi::class)
class CoroutinesTimeMachine : TestWatcher(), TimeMachine {
    val coroutineContext = TestCoroutineContext()

    override fun starting(description: Description?) {
        super.starting(description)

        TestableDispatchers.dispatcherOverride = { coroutineContext }
        JavaTimeProvider.currentTimeMillisProvider = { now }
        JavaTimeProvider.clockProvider =
            { Clock.fixed(Instant.ofEpochMilli(now), ZoneId.of("UTC")) }
    }

    override fun finished(description: Description?) {
        super.finished(description)

        TestableDispatchers.dispatcherOverride = { it() }
        JavaTimeProvider.currentTimeMillisProvider = { System.currentTimeMillis() }
        JavaTimeProvider.clockProvider = { Clock.systemUTC() }

        MultipleFailureException.assertEmpty(coroutineContext.exceptions)
    }

    /**
     * Moves the Scheduler's clock forward by a specified amount of milliseconds.
     */
    override fun advanceTime(ms: Long) {
        coroutineContext.advanceTimeBy(ms, TimeUnit.MILLISECONDS)
    }

    /**
     * Triggers any actions that have not yet been triggered and that are scheduled to be triggered at or
     * before this Scheduler's present time.
     */
    fun triggerActions() {
        coroutineContext.triggerActions()
    }

    override val now: Long
        get() = coroutineContext.now(TimeUnit.MILLISECONDS)
}