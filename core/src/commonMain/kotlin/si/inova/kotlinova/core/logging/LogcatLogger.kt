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

import si.inova.kotlinova.core.logging.LogcatLogger.Companion.install
import si.inova.kotlinova.core.logging.LogcatLogger.Companion.uninstall

/**
 * Logger that [logcat] delegates to. Call [install] to install a new logger, the default is a
 * no-op logger. Calling [uninstall] falls back to the default no-op logger.
 *
 * Adapted from https://github.com/square/logcat/blob/f877f806e8dce98a3abbc2ca80e6447b38cd8f42/logcat/src/main/java/logcat/LogcatLogger.kt
 */
@Deprecated(
   "Upstream logcat now supports KMP. Use that instead",
   replaceWith = ReplaceWith("LogcatLogger", "logcat.LogcatLogger")
)
interface LogcatLogger {

   /**
    * Whether a log with the provided priority should be logged and the corresponding message
    * providing lambda evaluated. Called by [logcat].
    */
   fun isLoggable(priority: LogPriority) = true

   /**
    * Write a log to its destination. Called by [logcat].
    */
   fun log(
      priority: LogPriority,
      tag: String,
      message: String,
   )

   companion object {
      @Volatile
      @PublishedApi
      internal var logger: LogcatLogger = NoLog
         private set

      @Volatile
      private var installedThrowable: Throwable? = null

      val isInstalled: Boolean
         get() = installedThrowable != null

      /**
       * Installs a [LogcatLogger].
       *
       * It is an error to call [install] more than once without calling [uninstall] in between,
       * however doing this won't throw, it'll log an error to the newly provided logger.
       */
      fun install(logger: LogcatLogger) {
         synchronized(this) {
            if (isInstalled) {
               logger.log(
                  LogPriority.ERROR,
                  "LogcatLogger",
                  "Installing $logger even though a logger was previously installed here: " +
                     requireNotNull(installedThrowable).asLog()
               )
            }
            installedThrowable = RuntimeException("Previous logger installed here")
            Companion.logger = logger
         }
      }

      /**
       * Replaces the current logger (if any) with a no-op logger.
       */
      fun uninstall() {
         synchronized(this) {
            installedThrowable = null
            logger = NoLog
         }
      }
   }

   private object NoLog : LogcatLogger {
      override fun isLoggable(priority: LogPriority) = false

      override fun log(
         priority: LogPriority,
         tag: String,
         message: String,
      ) = error("Should never receive any log")
   }
}
