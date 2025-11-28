/*
 * Copyright 2025 INOVA IT d.o.o.
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

package si.inova.kotlinova.core.logging

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import si.inova.kotlinova.core.logging.AndroidLogcatLogger.Companion.installOnDebuggableApp
import kotlin.math.min

/**
 * A [logcat] logger that delegates to [android.util.Log] for any log with a priority of
 * at least [minPriorityInt], and is otherwise a no-op.
 *
 * Handles special cases for [LogPriority.ASSERT] (which requires sending to Log.wtf) and
 * splitting logs to be at most 4000 characters per line (otherwise logcat just truncates).
 *
 * Call [installOnDebuggableApp] to make sure you never log in release builds.
 *
 * From https://github.com/square/logcat/blob/f877f806e8dce98a3abbc2ca80e6447b38cd8f42/logcat/src/main/java/logcat/Logcat.kt
 *
 * Copied into this project to allow access from both android and pure jvm projects
 */
@Deprecated(
   "Upstream logcat now supports KMP. Use that instead",
   replaceWith = ReplaceWith("AndroidLogcatLogger", "logcat.AndroidLogcatLogger")
)
class AndroidLogcatLogger(minPriority: LogPriority = LogPriority.DEBUG) : LogcatLogger {

   private val minPriorityInt: Int = minPriority.priorityInt

   override fun isLoggable(priority: LogPriority): Boolean =
      priority.priorityInt >= minPriorityInt

   override fun log(
      priority: LogPriority,
      tag: String,
      message: String
   ) {
      // Tag length limit was removed in API 26.
      val trimmedTag = if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         tag
      } else {
         tag.substring(0, MAX_TAG_LENGTH)
      }

      if (message.length < MAX_LOG_LENGTH) {
         logToLogcat(priority.priorityInt, trimmedTag, message)
         return
      }

      // Split by line, then ensure each line can fit into Log's maximum length.
      var i = 0
      val length = message.length
      while (i < length) {
         var newline = message.indexOf('\n', i)
         newline = if (newline != -1) newline else length
         do {
            val end = min(newline, i + MAX_LOG_LENGTH)
            val part = message.substring(i, end)
            logToLogcat(priority.priorityInt, trimmedTag, part)
            i = end
         } while (i < newline)
         i++
      }
   }

   private fun logToLogcat(
      priority: Int,
      tag: String,
      part: String
   ) {
      if (priority == Log.ASSERT) {
         Log.wtf(tag, part)
      } else {
         Log.println(priority, tag, part)
      }
   }

   companion object {
      fun installOnDebuggableApp(application: Application, minPriority: LogPriority = LogPriority.DEBUG) {
         if (!LogcatLogger.isInstalled && application.isDebuggableApp) {
            LogcatLogger.install(AndroidLogcatLogger(minPriority))
         }
      }
   }
}

private const val MAX_LOG_LENGTH = 4000
private const val MAX_TAG_LENGTH = 23

private val Application.isDebuggableApp: Boolean
   get() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
