package si.inova.kotlinova.time

import android.os.SystemClock
import android.support.annotation.RestrictTo
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Time provider for Android system time and backported ThreeTen time objects
 *
 * All methods in this class are overridable, allowing for mocking time
 *
 * @author Matej Drobnic
 */
object AndroidTimeProvider {
    /**
     * @see SystemClock.elapsedRealtime
     */
    fun elapsedRealtime(): Long = elapsedRealtimeProvider()

    /**
     * @see SystemClock.uptimeMillis
     */
    fun uptimeMillis(): Long = uptimeMillisProvider()

    fun currentInstant(): Instant =
        Instant.now(clockProvider())

    fun todayLocalDate(): LocalDate = LocalDate.now(clockProvider())

    fun currentLocalDateTime(): LocalDateTime = LocalDateTime.now(clockProvider())

    @RestrictTo(RestrictTo.Scope.TESTS)
    var elapsedRealtimeProvider = { SystemClock.elapsedRealtime() }
    @RestrictTo(RestrictTo.Scope.TESTS)
    var uptimeMillisProvider = { SystemClock.uptimeMillis() }
    @RestrictTo(RestrictTo.Scope.TESTS)
    var clockProvider = { Clock.systemUTC() }
}