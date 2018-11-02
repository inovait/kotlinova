/**
 * @author Matej Drobnic
 */
@file:JvmName("AndroidTestUtils")

package si.inova.kotlinova.testing

import android.arch.lifecycle.LiveData
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.first
import org.robolectric.shadows.ShadowSystemClock
import si.inova.kotlinova.coroutines.UI
import si.inova.kotlinova.coroutines.toChannel
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.time.JavaTimeProvider
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
    return GlobalScope.async(UI, CoroutineStart.DEFAULT, { toChannel().first(predicate) })
}

fun calendarFromDate(day: Int, month: Int, year: Int): Calendar {
    return JavaTimeProvider.currentCalendar()
        .apply { set(year, month - 1, day, 0, 0, 0) }
}

fun isoFromDate(day: Int, month: Int, year: Int): String {
    return SimpleDateFormat(
        ISO_8601_FORMAT_STRING_WITHOUT_TZ,
        Locale.getDefault()
    )
        .format(calendarFromDate(day, month, year).time)
}

suspend fun <T> LiveData<Resource<T>>.awaitSuccessAndThrowErrors(): T {
    val winResource = toChannel().consume {
        first {
            if (it is Resource.Error) {
                throw it.exception
            }

            it is Resource.Success
        }
    } ?: throw NoSuchElementException()

    return (winResource as Resource.Success).data
}