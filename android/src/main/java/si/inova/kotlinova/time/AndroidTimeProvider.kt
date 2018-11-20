package si.inova.kotlinova.time

import android.os.SystemClock
import androidx.annotation.RestrictTo
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset

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
        Instant.now(clockProvider(ZoneOffset.UTC))

    fun todayLocalDate(): LocalDate = LocalDate.now(clockProvider(ZoneId.systemDefault()))

    fun currentLocalDateTime(): LocalDateTime =
        LocalDateTime.now(clockProvider(ZoneId.systemDefault()))

    @RestrictTo(RestrictTo.Scope.TESTS)
    var elapsedRealtimeProvider = { SystemClock.elapsedRealtime() }
    @RestrictTo(RestrictTo.Scope.TESTS)
    var uptimeMillisProvider = { SystemClock.uptimeMillis() }
    @RestrictTo(RestrictTo.Scope.TESTS)
    var clockProvider: (ZoneId) -> Clock = { Clock.system(it) }
}