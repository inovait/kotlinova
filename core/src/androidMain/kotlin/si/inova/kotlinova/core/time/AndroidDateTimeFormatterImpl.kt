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

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import si.inova.kotlinova.core.reporting.ErrorReporter
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Provides Android-specific [DateTimeFormatter]s, such as a localized time formatter that respects the user's
 * 12-/24-hour clock preference.
 *
 * Adapted from https://github.com/drewhamilton/AndroidDateTimeFormatters/blob/9dee359a916a6d6e30bfa83613638ece0c3eb688/javatime/src/main/java/dev/drewhamilton/androidtime/format/AndroidDateTimeFormatter.java
 */
class AndroidDateTimeFormatterImpl @Inject constructor(
   private val context: Context,
   private val errorReporter: ErrorReporter
) : BaseAndroidDateTimeFormatter() {

   //endregion
   override fun getSystemTimeSettingAwareShortTimePattern(): String {
      val legacyFormat = DateFormat.getTimeFormat(context)
      return if (legacyFormat is SimpleDateFormat) {
         legacyFormat.toPattern()
      } else {
         errorReporter.report(IllegalStateException("Unknown date format type: $legacyFormat ${legacyFormat.javaClass}"))

         if (DateFormat.is24HourFormat(context)) {
            "HH:mm"
         } else {
            "hh:mm a"
         }
      }
   }

   override fun extractPrimaryLocale(): Locale {
      val configuration = context.resources.configuration
      var locale: Locale? = null

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
         val localeList = configuration.locales
         if (!localeList.isEmpty) {
            locale = localeList[0]
         }
      }

      if (locale == null) {
         @Suppress("DEPRECATION")
         locale = configuration.locale
      }

      if (locale == null) {
         locale = Locale.getDefault() ?: error("Default locale should never be null")
      }

      return locale
   }
}
