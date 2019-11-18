package si.inova.kotlinova.testing

import android.os.SystemClock
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import si.inova.kotlinova.time.AndroidTimeProvider
import si.inova.kotlinova.time.JavaTimeProvider

/**
 * JUnit Rule that controls all values in [JavaTimeProvider] and [AndroidTimeProvider]
 * based on Robolectric's scheduler
 * @author Matej Drobnic
 */
@Deprecated("Use instrumented tests instead of Robolectric")
class RobolectricTimeMachineRule : TestWatcher() {
    override fun finished(description: Description?) {
        JavaTimeProvider.currentTimeMillisProvider = { System.currentTimeMillis() }
        JavaTimeProvider.currentTimeMillisProvider = { SystemClock.elapsedRealtime() }
        AndroidTimeProvider.uptimeMillisProvider = { SystemClock.uptimeMillis() }
        AndroidTimeProvider.clockProvider = { Clock.systemUTC() }
    }

    override fun starting(description: Description?) {
        JavaTimeProvider.currentTimeMillisProvider = { SystemClock.elapsedRealtime() }
        JavaTimeProvider.currentTimeMillisProvider = { SystemClock.elapsedRealtime() }
        AndroidTimeProvider.uptimeMillisProvider = { SystemClock.elapsedRealtime() }
        AndroidTimeProvider.clockProvider =
            { Clock.fixed(Instant.ofEpochMilli(SystemClock.elapsedRealtime()), ZoneId.of("UTC")) }
    }
}