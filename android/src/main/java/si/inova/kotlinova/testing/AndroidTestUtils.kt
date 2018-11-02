/**
 * @author Matej Drobnic
 */
@file:JvmName("AndroidTestUtils")

package si.inova.kotlinova.testing

import android.arch.lifecycle.LiveData
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.first
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
    // Channel operators are obsolete, but there is no alternative at the moment
    // We will migrate when alternative will be added
    // See https://github.com/Kotlin/kotlinx.coroutines/issues/632
    @Suppress("EXPERIMENTAL_API_USAGE")

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
    // Channel operators are obsolete, but there is no alternative at the moment
    // We will migrate when alternative will be added
    // See https://github.com/Kotlin/kotlinx.coroutines/issues/632
    @Suppress("EXPERIMENTAL_API_USAGE")
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