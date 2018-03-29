@file:JvmName("TimeUtils")

package si.inova.kotlinova.utils

import android.content.Context
import si.inova.kotlinova.R
import java.util.Calendar

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