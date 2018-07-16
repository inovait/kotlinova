package si.inova.kotlinova.time

import android.os.SystemClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.util.Calendar
import java.util.Date

/**
 * System and CPU time provider that can be mocked for tests
 *
 * This has been deprecated and split into
 * java-pure *JavaTimeProvider* and Android's *AndroidTimeProvider*.
 *
 * @author Matej Drobnic
 */
@Deprecated("Use either JavaTimeProvider or AndroidTimeProvider")
object TimeProvider {
    /**
     * @see System.currentTimeMillis
     */
    @Deprecated(
        "Use JavaTimeProvider.currentTimeMillis()",
        ReplaceWith("JavaTimeProvider.currentTimeMillis()")
    )
    fun currentTimeMillis(): Long = JavaTimeProvider.currentTimeMillis()

    /**
     * @see SystemClock.elapsedRealtime
     */
    @Deprecated(
        "Use AndroidTimeProvider.elapsedRealtime()",
        ReplaceWith("AndroidTimeProvider.elapsedRealtime()")
    )
    fun elapsedRealtime(): Long = AndroidTimeProvider.elapsedRealtime()

    /**
     * @see SystemClock.uptimeMillis
     */
    @Deprecated(
        "Use AndroidTimeProvider.uptimeMillis()",
        ReplaceWith("AndroidTimeProvider.uptimeMillis()")
    )
    fun uptimeMillis(): Long = AndroidTimeProvider.uptimeMillis()

    @Deprecated(
        "Use JavaTimeProvider.currentDate()",
        ReplaceWith("JavaTimeProvider.currentDate()")
    )
    fun currentDate(): Date = JavaTimeProvider.currentDate()

    @Deprecated(
        "Use JavaTimeProvider.currentCalendar()",
        ReplaceWith("JavaTimeProvider.currentCalendar()")
    )
    fun currentCalendar(): Calendar = JavaTimeProvider.currentCalendar()

    @Deprecated(
        "Use AndroidTimeProvider.currentInstant()",
        ReplaceWith("AndroidTimeProvider.currentInstant()")
    )
    fun currentInstant(): Instant = AndroidTimeProvider.currentInstant()

    @Deprecated(
        "Use AndroidTimeProvider.todayLocalDate()",
        ReplaceWith("AndroidTimeProvider.todayLocalDate()")
    )
    fun todayLocalDate(): LocalDate = AndroidTimeProvider.todayLocalDate()

    @Deprecated(
        "Use AndroidTimeProvider.currentLocalDateTime()",
        ReplaceWith("AndroidTimeProvider.currentLocalDateTime()")
    )
    fun currentLocalDateTime(): LocalDateTime = AndroidTimeProvider.currentLocalDateTime()

    @Deprecated(
        "Use JavaTimeProvider.currentTimeMillisProvider",
        ReplaceWith("JavaTimeProvider.currentTimeMillisProvider")
    )
    var currentTimeMillisProvider
        get() = JavaTimeProvider.currentTimeMillisProvider
        set(value) {
            JavaTimeProvider.currentTimeMillisProvider = value
        }

    @Deprecated(
        "Use AndroidTimeProvider.elapsedRealtimeProvider",
        ReplaceWith("AndroidTimeProvider.elapsedRealtimeProvider")
    )
    var elapsedRealtimeProvider
        get() = AndroidTimeProvider.elapsedRealtimeProvider
        set(value) {
            AndroidTimeProvider.elapsedRealtimeProvider = value
        }

    @Deprecated(
        "Use AndroidTimeProvider.uptimeMillisProvider",
        ReplaceWith("AndroidTimeProvider.uptimeMillisProvider")
    )
    var uptimeMillisProvider
        get() = AndroidTimeProvider.uptimeMillisProvider
        set(value) {
            AndroidTimeProvider.uptimeMillisProvider = value
        }

    @Deprecated(
        "Use AndroidTimeProvider.clockProvider",
        ReplaceWith("AndroidTimeProvider.clockProvider")
    )
    var clockProvider
        get() = AndroidTimeProvider.clockProvider
        set(value) {
            AndroidTimeProvider.clockProvider = value
        }
}