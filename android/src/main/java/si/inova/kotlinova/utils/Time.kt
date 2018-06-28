@file:JvmName("TimeUtils")

package si.inova.kotlinova.utils

import android.content.Context
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.ResolverStyle
import org.threeten.bp.temporal.ChronoField
import si.inova.kotlinova.R
import java.util.Calendar
import java.util.Date

/**
 * @author Matej Drobnic
 */
fun Calendar.isSameDay(other: Calendar): Boolean {
    return get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH) &&
        get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
        get(Calendar.YEAR) == other.get(Calendar.YEAR)
}

object TimeFormat {
    fun toHoursMinutesSeconds(
        totalSeconds: Int,
        context: Context,
        alwaysShowHours: Boolean = true
    ): String {
        var leftSeconds = totalSeconds

        val hours = leftSeconds / 3600
        leftSeconds %= 3600

        val minutes = leftSeconds / 60
        leftSeconds %= 60

        return if (hours > 0 || alwaysShowHours) {
            context.getString(R.string.time_hours_minutes_seconds, hours, minutes, leftSeconds)
        } else {
            context.getString(R.string.time_minutes_seconds, minutes, leftSeconds)
        }
    }

    fun toMinutesSeconds(totalSeconds: Int, context: Context): String {
        var leftSeconds = totalSeconds

        val minutes = leftSeconds / 60
        leftSeconds %= 60

        return context.getString(R.string.time_minutes_seconds, minutes, leftSeconds)
    }
}

fun Date.toCalendar(): Calendar {
    return Calendar.getInstance().apply { time = this@toCalendar }
}

/**
 * Time formater similar to [DateTimeFormatter.ISO_LOCAL_TIME], except that it always
 * prints out seconds, even if those are zeros. It also never prints out nanoseconds.
 */
val ISO_LOCAL_TIME_WITH_FORCED_SECONDS by lazy {
    DateTimeFormatterBuilder()
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
        .toFormatter()
        .withResolverStyle(ResolverStyle.STRICT)
}

val ISO_LOCAL_DATE_TIME_WITH_FORCED_SECONDS by lazy {
    DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_DATE)
        .appendLiteral('T')
        .append(ISO_LOCAL_TIME_WITH_FORCED_SECONDS)
        .toFormatter()
        .withResolverStyle(ResolverStyle.STRICT)
        .withChronology(IsoChronology.INSTANCE)
}

const val ISO_8601_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssZ"
const val ISO_8601_FORMAT_STRING_WITHOUT_TZ = "yyyy-MM-dd'T'HH:mm:ss"