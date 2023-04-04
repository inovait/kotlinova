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

package si.inova.kotlinova.compose.androidtest.time

import androidx.compose.ui.test.junit4.ComposeTestRule
import si.inova.kotlinova.core.time.AndroidTimeProvider
import si.inova.kotlinova.core.time.FakeAndroidTimeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * An [AndroidTimeProvider] instance that provides time based on virtual clock of this compose test.
 */
fun ComposeTestRule.virtualTimeProvider(
   currentLocalDate: () -> LocalDate = { LocalDate.MIN },
   currentLocalTime: () -> LocalTime = { LocalTime.MIN },
   currentLocalDateTime: () -> LocalDateTime = { LocalDateTime.of(currentLocalDate(), currentLocalTime()) },
   currentTimezone: () -> ZoneId = { ZoneId.of("UTC") },
   currentZonedDateTime: (() -> ZonedDateTime)? = null
): AndroidTimeProvider {
   return FakeAndroidTimeProvider(
      currentLocalDate,
      currentLocalTime,
      currentLocalDateTime,
      currentTimezone,
      currentZonedDateTime = currentZonedDateTime ?: {
         ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(mainClock.currentTime),
            currentTimezone()
         )
      },
   ) { mainClock.currentTime }
}
