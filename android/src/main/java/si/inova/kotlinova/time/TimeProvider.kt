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