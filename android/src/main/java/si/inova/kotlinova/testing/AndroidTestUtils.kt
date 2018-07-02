/**
 * @author Matej Drobnic
 */
@file:JvmName("AndroidTestUtils")

package si.inova.kotlinova.testing

import android.arch.lifecycle.LiveData
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.first
import org.robolectric.shadows.ShadowSystemClock
import si.inova.kotlinova.coroutines.UI
import si.inova.kotlinova.coroutines.toChannel
import si.inova.kotlinova.time.TimeProvider
import si.inova.kotlinova.utils.ISO_8601_FORMAT_STRING_WITHOUT_TZ
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun advanceTime(ms: Int) {
    advanceTime(ms.toLong())
}

fun advanceTime(ms: Long) {
    ShadowSystemClock.sleep(ms)
}

suspend fun <T> LiveData<T>.waitUntil(predicate: (T?) -> Boolean): Deferred<T?> {
    return async(UI) { toChannel().first(predicate) }
}

fun calendarFromDate(day: Int, month: Int, year: Int): Calendar {
    return TimeProvider.currentCalendar()
        .apply { set(year, month - 1, day, 0, 0, 0) }
}

fun isoFromDate(day: Int, month: Int, year: Int): String {
    return SimpleDateFormat(
        ISO_8601_FORMAT_STRING_WITHOUT_TZ,
        Locale.getDefault()
    )
        .format(calendarFromDate(day, month, year).time)
}