package si.inova.kotlinova.testing

import android.os.SystemClock
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import si.inova.kotlinova.time.TimeProvider

/**
 * JUnit Rule that controls all values in [TimeProvider] based on Robolectric's scheduler
 * @author Matej Drobnic
 */
class RobolectricTimeMachineRule : TestWatcher() {
    override fun finished(description: Description?) {
        TimeProvider.currentTimeMillisProvider = { System.currentTimeMillis() }
        TimeProvider.elapsedRealtimeProvider = { SystemClock.elapsedRealtime() }
        TimeProvider.uptimeMillisProvider = { SystemClock.uptimeMillis() }
        TimeProvider.clockProvider = { Clock.systemUTC() }
    }

    override fun starting(description: Description?) {
        TimeProvider.currentTimeMillisProvider = { SystemClock.elapsedRealtime() }
        TimeProvider.elapsedRealtimeProvider = { SystemClock.elapsedRealtime() }
        TimeProvider.uptimeMillisProvider = { SystemClock.elapsedRealtime() }
        TimeProvider.clockProvider =
            { Clock.fixed(Instant.ofEpochMilli(SystemClock.elapsedRealtime()), ZoneId.of("UTC")) }
    }
}