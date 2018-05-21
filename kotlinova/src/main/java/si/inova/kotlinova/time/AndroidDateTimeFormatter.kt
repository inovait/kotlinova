package si.inova.kotlinova.time

import android.content.Context
import android.text.format.DateFormat
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat

/**
 * Bridge class that returns [DateTimeFormatter] classes for Android's local time format
 *
 * @author Matej Drobnic
 */
object AndroidDateTimeFormatter {
    @JvmStatic
    fun getTimeInstance(context: Context): DateTimeFormatter {
        val oldFormat = DateFormat.getTimeFormat(context) as SimpleDateFormat
        return DateTimeFormatter.ofPattern(oldFormat.toPattern())
    }
}