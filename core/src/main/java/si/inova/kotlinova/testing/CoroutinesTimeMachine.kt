package si.inova.kotlinova.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.TestableDispatchers
import si.inova.kotlinova.time.JavaTimeProvider
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * JUnit Rule that installs and controls [TestCoroutineDispatcher] and
 * [JavaTimeProvider] at once.
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class CoroutinesTimeMachine : TestWatcher(), TimeMachine {
    val coroutineContext = TestCoroutineDispatcher()

    override fun starting(description: Description?) {
        super.starting(description)

        TestableDispatchers.dispatcherOverride = { coroutineContext }
        Dispatchers.setMain(coroutineContext)
        JavaTimeProvider.currentTimeMillisProvider = { now }
        JavaTimeProvider.clockProvider =
            { Clock.fixed(Instant.ofEpochMilli(now), ZoneId.of("UTC")) }

        coroutineContext.pauseDispatcher()
    }

    override fun finished(description: Description?) {
        super.finished(description)

        TestableDispatchers.dispatcherOverride = { it() }
        Dispatchers.resetMain()
        JavaTimeProvider.currentTimeMillisProvider = { System.currentTimeMillis() }
        JavaTimeProvider.clockProvider = { Clock.systemUTC() }
    }

    /**
     * Moves the Scheduler's clock forward by a specified amount of milliseconds.
     */
    override fun advanceTime(ms: Long) {
        coroutineContext.advanceTimeBy(ms)
    }

    /**
     * Triggers any actions that have not yet been triggered and that are scheduled to be triggered at or
     * before this Scheduler's present time.
     */
    fun triggerActions() {
        coroutineContext.runCurrent()
    }

    override val now: Long
        get() = coroutineContext.currentTime
}