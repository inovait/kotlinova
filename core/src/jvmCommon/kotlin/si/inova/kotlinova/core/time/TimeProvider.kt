/*
 * Copyright 2023 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.kotlinova.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Injectable interface that provides current time
 */
interface TimeProvider {
   /**
    * @return difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
    *
    * @see System.currentTimeMillis
    */
   fun currentTimeMillis(): Long

   /**
    * The value returned represents milliseconds since some fixed but arbitrary origin time
    * (perhaps in the future, so values may be negative).
    *
    * This is not related to any other notion of system or wall-clock time,
    * meaning the value will not change if user changes system time settings. That's why
    * it is recommended to use this to measure elapsed time instead of [currentTimeMillis].
    *
    * This value should not be stored persistently between reboots / process instances. It should be kept in-memory only.
    */
   fun currentMonotonicTimeMillis(): Long

   fun currentLocalDate(): LocalDate

   fun currentLocalDateTime(): LocalDateTime

   fun currentLocalTime(): LocalTime

   fun currentInstant(): Instant

   fun currentZonedDateTime(): ZonedDateTime

   fun systemDefaultZoneId(): ZoneId
}
