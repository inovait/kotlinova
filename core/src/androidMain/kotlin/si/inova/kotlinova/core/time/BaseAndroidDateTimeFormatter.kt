/*
 * Copyright 2024 INOVA IT d.o.o.
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

import android.annotation.SuppressLint
import android.text.format.DateFormat
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Provides Android-specific [DateTimeFormatter]s, such as a localized time formatter that respects the user's
 * 12-/24-hour clock preference.
 *
 * Adapted from https://github.com/drewhamilton/AndroidDateTimeFormatters/blob/9dee359a916a6d6e30bfa83613638ece0c3eb688/javatime/src/main/java/dev/drewhamilton/androidtime/format/AndroidDateTimeFormatter.java
 */
@SuppressLint("NewApi") // java.time is desugared, so we don't need later SDK, https://issuetracker.google.com/issues/347167978
abstract class BaseAndroidDateTimeFormatter : AndroidDateTimeFormatter {

   /**
    * Returns a [DateTimeFormatter] that can format the time according to the context's locale.
    * If `timeStyle` is [FormatStyle.SHORT], the formatter also respects the user's 12-/24-hour clock preference.
    *
    * The [FormatStyle.FULL] and [FormatStyle.LONG] styles typically require a time-zone. When formatting
    * using these styles, a [java.time.ZoneId] must be available, either by using [java.time.ZonedDateTime]
    * or [DateTimeFormatter.withZone].
    *
    * @return the time formatter
    */

   override fun ofLocalizedTime(): DateTimeFormatter {
      val contextPrimaryLocale = extractPrimaryLocale()

      val pattern = getSystemTimeSettingAwareShortTimePattern()
      return DateTimeFormatterBuilder()
         .appendPattern(pattern)
         .toFormatter(contextPrimaryLocale) // Match java.time's ofLocalizedTime, which also hard-codes IsoChronology:
         .withChronology(IsoChronology.INSTANCE)
   }
   //endregion
   //region ofLocalizedDate
   /**
    * Returns a locale specific date format for the ISO chronology.
    *
    *
    * This returns a formatter that will format or parse a date. The exact format pattern used varies by locale.
    *
    *
    * The locale is determined from the formatter. The formatter returned directly by this method will use the provided
    * Context's primary locale.
    *
    *
    * Note that the localized pattern is looked up lazily. This `DateTimeFormatter` holds the style required and
    * the locale, looking up the pattern required on demand.
    *
    *
    * The returned formatter has a chronology of ISO set to ensure dates in other calendar systems are correctly
    * converted. It has no override zone and uses the [SMART][java.time.format.ResolverStyle.SMART] resolver
    * style.
    *
    * @param dateStyle the formatter style to obtain
    * @return the date formatter
    */
   override fun ofLocalizedDate(dateStyle: FormatStyle): DateTimeFormatter {
      return DateTimeFormatter.ofLocalizedDate(dateStyle)
         .withLocale(extractPrimaryLocale())
   }
   //endregion
   //region ofLocalizedDateTime
   /**
    * Returns a locale specific date and time format for the ISO chronology.
    *
    *
    * This returns a formatter that will format or parse a date-time. The exact format pattern used varies by locale.
    *
    *
    * The locale is determined from the formatter. The formatter returned directly by this method will use the provided
    * context's primary locale.
    *
    *
    * Note that the localized pattern is looked up lazily. This `DateTimeFormatter` holds the style required and
    * the locale, looking up the pattern required on demand.
    *
    *
    * The returned formatter has a chronology of ISO set to ensure dates in other calendar systems are correctly
    * converted. It has no override zone and uses the [SMART][java.time.format.ResolverStyle.SMART] resolver
    * style.
    *
    * @param dateStyle the date formatter style to obtain
    * @return the date, time or date-time formatter
    */
   override fun ofLocalizedDateTime(
      dateStyle: FormatStyle
   ): DateTimeFormatter {
      // If time format is SHORT, try system 12-/24-hour setting-specific time format:
      return attemptSystemSettingDateTimeFormatter(dateStyle)
   }

   private fun attemptSystemSettingDateTimeFormatter(
      dateStyle: FormatStyle,
   ): DateTimeFormatter {
      val timePattern = getSystemTimeSettingAwareShortTimePattern()
      val contextPrimaryLocale = extractPrimaryLocale()
      val dateFormatter = DateTimeFormatter.ofLocalizedDate(dateStyle)

      return DateTimeFormatterBuilder()
         .append(dateFormatter)
         .appendLiteral(' ')
         .appendPattern(timePattern)
         .toFormatter(contextPrimaryLocale) // Match java.time's ofLocalizedDateTime, which also hard-codes IsoChronology:
         .withChronology(IsoChronology.INSTANCE)
   }

   //endregion
   //region ofSkeleton
   /**
    * Returns the best possible localized formatter of the given skeleton for the given context's primary locale. A
    * skeleton is similar to, and uses the same format characters as, a Unicode
    * [UTS #35](http://www.unicode.org/reports/tr35/#Date_Format_Patterns) pattern.
    *
    *
    * One difference is that order is irrelevant. For example, "MMMMd" will become "MMMM d" in the `en_US`
    * locale, but "d. MMMM" in the `de_CH` locale.
    *
    *
    * Note also in that second example that the necessary punctuation for German was added. For the same input in
    * `es_ES`, we'd have even more extra text: "d 'de' MMMM".
    *
    *
    * This method will automatically correct for grammatical necessity. Given the same "MMMMd" input, the formatter
    * will use "d LLLL" in the `fa_IR` locale, where stand-alone months are necessary. **Warning: core
    * library desugaring does not currently support formatting with 'L'.**
    *
    *
    * Lengths are preserved where meaningful, so "Md" would give a different result to "MMMd", say, except in a
    * locale such as `ja_JP` where there is only one length of month.
    *
    *
    * This method will only use patterns that are in CLDR, and is useful whenever you know what elements you want
    * in your format string but don't want to make your code specific to any one locale.
    *
    * "TTT" inside skeleton will get formatted as either 12 hour time (hh:MM) or 24 hour time (HH:mm a),
    * depending on the user's locale settings
    *
    * @param skeleton a skeleton as described above
    * @return a formatter with the localized pattern based on the skeleton
    */
   override fun ofSkeleton(skeleton: String): DateTimeFormatter {
      val skeletonWithFixedShortTime = skeleton.replace("TTT", getSystemTimeSettingAwareShortTimePattern())
      return ofSkeleton(skeletonWithFixedShortTime, extractPrimaryLocale())
   }

   /**
    * Returns the best possible localized formatter of the given skeleton for the given locale. A skeleton is similar
    * to, and uses the same format characters as, a Unicode
    * [UTS #35](http://www.unicode.org/reports/tr35/#Date_Format_Patterns) pattern.
    *
    *
    * One difference is that order is irrelevant. For example, "MMMMd" will become "MMMM d" in the `en_US`
    * locale, but "d. MMMM" in the `de_CH` locale.
    *
    *
    * Note also in that second example that the necessary punctuation for German was added. For the same input in
    * `es_ES`, we'd have even more extra text: "d 'de' MMMM".
    *
    *
    * This method will automatically correct for grammatical necessity. Given the same "MMMMd" input, the formatter
    * will use "d LLLL" in the `fa_IR` locale, where stand-alone months are necessary. **Warning: core
    * library desugaring does not currently support formatting with 'L'.**
    *
    *
    * Lengths are preserved where meaningful, so "Md" would give a different result to "MMMd", say, except in a
    * locale such as `ja_JP` where there is only one length of month.
    *
    * TTT format will get converted into user-set 12 hour or 24 hour time.
    *
    * This method will only use patterns that are in CLDR, and is useful whenever you know what elements you want
    * in your format string but don't want to make your code specific to any one locale.
    *
    * @param skeleton a skeleton as described above
    * @param locale the locale into which the skeleton should be localized
    * @return a formatter with the localized pattern based on the skeleton
    */
   override fun ofSkeleton(skeleton: String, locale: Locale): DateTimeFormatter {
      val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
      return DateTimeFormatter.ofPattern(pattern, locale)
   }

   //endregion
   abstract fun extractPrimaryLocale(): Locale
   abstract fun getSystemTimeSettingAwareShortTimePattern(): String
}
