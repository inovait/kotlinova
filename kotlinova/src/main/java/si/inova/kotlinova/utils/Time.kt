@file:JvmName("TimeUtils")

package si.inova.kotlinova.utils

import android.content.Context
import si.inova.kotlinova.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * @author Matej Drobnic
 */
fun Calendar.isSameDay(other: Calendar): Boolean {
    return get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH) &&
            get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
            get(Calendar.YEAR) == other.get(Calendar.YEAR)
}

object TimeFormat {
    fun toHoursMinutesSeconds(totalSeconds: Int, context: Context): String {
        var leftSeconds = totalSeconds

        val hours = leftSeconds / 3600
        leftSeconds %= 3600

        val minutes = leftSeconds / 60
        leftSeconds %= 60

        return context.getString(R.string.time_hours_minutes_seconds, hours, minutes, leftSeconds)
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

const val ISO_8601_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
// SimpleDateFormat is not thread-safe. Use ThreadLocal to bind it to separate threads.
val ISO_8601_DATE_FORMAT_HOLDER =
        threadLocal { SimpleDateFormat(ISO_8601_FORMAT_STRING, Locale.getDefault()) }

inline val ISO_8601_DATE_FORMAT: DateFormat
    get() = ISO_8601_DATE_FORMAT_HOLDER.get()