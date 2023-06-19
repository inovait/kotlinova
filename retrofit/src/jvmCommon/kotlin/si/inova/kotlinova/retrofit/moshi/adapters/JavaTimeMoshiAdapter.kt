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

package si.inova.kotlinova.retrofit.moshi.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Moshi adapter for converting java.time's [Instant], [LocalTime], [LocalDate], [LocalDateTime] and [ZonedDateTime] to
 * ISO String for JSON serialization.
 */
object JavaTimeMoshiAdapter {
   @ToJson
   fun fromInstantToString(value: Instant): String {
      return DateTimeFormatter.ISO_INSTANT.format(value)
   }

   @FromJson
   fun fromStringToInstant(jsonValue: String): Instant {
      return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(jsonValue))
   }

   @ToJson
   fun fromLocalTimeToString(value: LocalTime): String {
      return DateTimeFormatter.ISO_LOCAL_TIME.format(value)
   }

   @FromJson
   fun fromStringToLocalTime(jsonValue: String): LocalTime {
      return LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(jsonValue))
   }

   @ToJson
   fun fromLocalDateToString(value: LocalDate): String {
      return DateTimeFormatter.ISO_LOCAL_DATE.format(value)
   }

   @FromJson
   fun fromStringToLocalDate(jsonValue: String): LocalDate {
      return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(jsonValue))
   }

   @ToJson
   fun fromLocalDateTimeToString(value: LocalDateTime): String {
      return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value)
   }

   @FromJson
   fun fromStringToLocalDateTime(jsonValue: String): LocalDateTime {
      return LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(jsonValue))
   }

   @ToJson
   fun fromZonedDateTimeToString(value: ZonedDateTime): String {
      return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(value)
   }

   @FromJson
   fun fromStringToZonedDateTime(jsonValue: String): ZonedDateTime {
      return ZonedDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(jsonValue))
   }
}
