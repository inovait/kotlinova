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

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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
        Instant.now(clockProvider(ZoneOffset.UTC))

    fun todayLocalDate(): LocalDate = LocalDate.now(clockProvider(ZoneId.systemDefault()))

    fun currentLocalDateTime(): LocalDateTime =
        LocalDateTime.now(clockProvider(ZoneId.systemDefault()))

    var currentTimeMillisProvider = { System.currentTimeMillis() }
    var clockProvider: (ZoneId) -> Clock = { Clock.system(it) }
}