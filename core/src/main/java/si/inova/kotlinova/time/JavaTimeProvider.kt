package si.inova.kotlinova.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date

/**
 * Time provider for raw unix time and for Java 8 time objects
 *
 * @author Matej Drobnic
 */
object JavaTimeProvider {
    /**
     * @see System.currentTimeMillis
     */
    fun currentTimeMillis(): Long = currentTimeMillisProvider()

    fun currentDate(): Date = Date(currentTimeMillis())
    fun currentCalendar(): Calendar =
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis() }

    fun currentInstant(): Instant =
        Instant.now(clockProvider())

    fun todayLocalDate(): LocalDate = LocalDate.now(clockProvider())

    fun currentLocalDateTime(): LocalDateTime = LocalDateTime.now(clockProvider())

    var currentTimeMillisProvider = { System.currentTimeMillis() }
    var clockProvider = { Clock.systemUTC() }
}