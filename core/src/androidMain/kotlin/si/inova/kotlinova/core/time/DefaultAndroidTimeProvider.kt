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

import android.os.SystemClock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object DefaultAndroidTimeProvider : AndroidTimeProvider {
   override fun currentTimeMillis(): Long {
      return System.currentTimeMillis()
   }

   override fun currentMonotonicTimeMillis(): Long {
      return SystemClock.elapsedRealtime()
   }

   override fun currentLocalDate(): LocalDate {
      return LocalDate.now()
   }

   override fun currentLocalDateTime(): LocalDateTime {
      return LocalDateTime.now()
   }

   override fun currentLocalTime(): LocalTime {
      return LocalTime.now()
   }

   override fun currentInstant(): Instant {
      return Instant.now()
   }

   override fun currentZonedDateTime(): ZonedDateTime {
      return ZonedDateTime.now()
   }

   override fun systemDefaultZoneId(): ZoneId {
      return ZoneId.systemDefault()
   }

   override fun elapsedRealtime(): Long {
      return SystemClock.elapsedRealtime()
   }

   override fun elapsedRealtimeNanos(): Long {
      return SystemClock.elapsedRealtimeNanos()
   }

   override fun uptimeMillis(): Long {
      return SystemClock.uptimeMillis()
   }
}
