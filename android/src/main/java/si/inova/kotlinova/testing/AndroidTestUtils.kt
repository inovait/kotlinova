/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

/**
 * @author Matej Drobnic
 */
@file:JvmName("AndroidTestUtils")

package si.inova.kotlinova.testing

import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.first
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import si.inova.kotlinova.archcomponents.AlwaysActiveLifecycleOwner
import si.inova.kotlinova.coroutines.toChannel
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.time.JavaTimeProvider
import si.inova.kotlinova.utils.ISO_8601_FORMAT_STRING_WITHOUT_TZ
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@VisibleForTesting
@Deprecated("Do not use Robolectric. Migrate to regular unit tests or instrumented tests.")
fun advanceTime(ms: Int) {
    advanceTime(ms.toLong())
}

@VisibleForTesting
@Deprecated("Do not use Robolectric. Migrate to regular unit tests or instrumented tests.")
fun advanceTime(ms: Long) {
    SystemClock.sleep(ms)
}

@VisibleForTesting
suspend fun <T> LiveData<T>.waitUntil(predicate: (T?) -> Boolean): Deferred<T?> {
    // Channel operators are obsolete, but there is no alternative at the moment
    // We will migrate when alternative will be added
    // See https://github.com/Kotlin/kotlinx.coroutines/issues/632
    @Suppress("EXPERIMENTAL_API_USAGE")

    return GlobalScope
        .async(Dispatchers.Main, CoroutineStart.DEFAULT, { toChannel().first(predicate) })
}

@VisibleForTesting
fun calendarFromDate(day: Int, month: Int, year: Int): Calendar {
    return JavaTimeProvider.currentCalendar()
        .apply { set(year, month - 1, day, 0, 0, 0) }
}

@VisibleForTesting
fun isoFromDate(day: Int, month: Int, year: Int): String {
    return SimpleDateFormat(
        ISO_8601_FORMAT_STRING_WITHOUT_TZ,
        Locale.getDefault()
    )
        .format(calendarFromDate(day, month, year).time)
}

@VisibleForTesting
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

@VisibleForTesting
fun <T> T.lazy(): dagger.Lazy<T> {
    return dagger.Lazy { this }
}

@VisibleForTesting
fun instantFromUtcTime(
    year: Int = 2000,
    month: Int = 1,
    dayOfMonth: Int = 1,
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    nanoOfSecond: Int = 0
): Instant {
    return ZonedDateTime.of(
        year, month, dayOfMonth, hour, minute, second, nanoOfSecond, ZoneId.of("UTC")
    ).toInstant()
}

@VisibleForTesting
fun instantFromIsoTimestamp(
    timestamp: String
): Instant {
    return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timestamp))
}

@VisibleForTesting
fun <T> LiveData<T>.testSubscribe(): TestSubscriber<T> {
    return Flowable.fromPublisher(
        LiveDataReactiveStreams.toPublisher(AlwaysActiveLifecycleOwner, this)
    )
        .test()
}