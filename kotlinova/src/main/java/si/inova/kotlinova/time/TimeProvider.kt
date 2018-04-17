package si.inova.kotlinova.time

import android.os.SystemClock
import android.support.annotation.RestrictTo
import java.util.Calendar
import java.util.Date

/**
 * System and CPU time provider that can be mocked for tests
 *
 * @author Matej Drobnic
 */
object TimeProvider {
    /**
     * @see System.currentTimeMillis
     */
    fun currentTimeMillis(): Long = currentTimeMillisProvider()

    /**
     * @see SystemClock.elapsedRealtime
     */
    fun elapsedRealtime(): Long = elapsedRealtimeProvider()

    /**
     * @see SystemClock.uptimeMillis
     */
    fun uptimeMillis(): Long = uptimeMillisProvider()

    fun currentDate(): Date = Date(currentTimeMillis())
    fun currentCalendar(): Calendar =
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis() }

    @RestrictTo(RestrictTo.Scope.TESTS)
    var currentTimeMillisProvider = { System.currentTimeMillis() }
    @RestrictTo(RestrictTo.Scope.TESTS)
    var elapsedRealtimeProvider = { SystemClock.elapsedRealtime() }
    @RestrictTo(RestrictTo.Scope.TESTS)
    var uptimeMillisProvider = { SystemClock.uptimeMillis() }
}