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

package si.inova.kotlinova.core.test.time

import si.inova.kotlinova.core.time.TimeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Time provider that provides fake pre-determined time. Use for tests.
 */
class FakeTimeProvider(
   private val currentLocalDate: () -> LocalDate = { LocalDate.MIN },
   private val currentLocalTime: () -> LocalTime = { LocalTime.MIN },
   private val currentLocalDateTime: () -> LocalDateTime = { LocalDateTime.of(currentLocalDate(), currentLocalTime()) },
   private val currentTimezone: () -> ZoneId = { ZoneId.of("UTC") },
   private val currentZonedDateTime: () -> ZonedDateTime = { ZonedDateTime.of(currentLocalDateTime(), currentTimezone()) },
   private val currentMilliseconds: () -> Long = { 0 }
) : TimeProvider {
   override fun currentTimeMillis(): Long {
      return currentMilliseconds()
   }

   override fun currentLocalDate(): LocalDate {
      return currentLocalDate.invoke()
   }

   override fun currentLocalDateTime(): LocalDateTime {
      return currentLocalDateTime.invoke()
   }

   override fun currentLocalTime(): LocalTime {
      return currentLocalTime.invoke()
   }

   override fun currentInstant(): Instant {
      return Instant.ofEpochMilli(currentTimeMillis())
   }

   override fun currentZonedDateTime(): ZonedDateTime {
      return currentZonedDateTime.invoke()
   }

   override fun systemDefaultZoneId(): ZoneId {
      return currentTimezone()
   }
}
