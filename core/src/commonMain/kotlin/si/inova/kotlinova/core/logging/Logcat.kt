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

/**
 * From https://github.com/square/logcat/blob/f877f806e8dce98a3abbc2ca80e6447b38cd8f42/logcat/src/main/java/logcat/Logcat.kt
 *
 * Copied into this project to allow access from both android and pure jvm projects
 */

package si.inova.kotlinova.core.logging

/**
 * A tiny Kotlin API for cheap logging on top of Android's normal `Log` class.
 *
 * The [logcat] function has 3 parameters: an optional [priority], an optional [tag], and a required
 * string producing lambda ([message]). The lambda is only evaluated if a logger is installed and
 * the logger deems the priority loggable.
 *
 * The priority defaults to [LogPriority.DEBUG].
 *
 * The tag defaults to the class name of the log call site, without any extra runtime cost. This works
 * because [logcat] is an inlined extension function of [Any] and has access to [this] from which
 * it can extract the class name. If logging from a standalone function which has no [this], use the
 * [logcat] overload which requires a tag parameter.
 *
 * The [logcat] function does not take a [Throwable] parameter. Instead, the library provides
 * a Throwable extension function: [Throwable.asLog] which returns a loggable string.
 *
 * ```
 * import logcat.LogPriority.INFO
 * import logcat.asLog
 * import logcat.logcat
 *
 * class MouseController {
 *
 *   fun play {
 *     var state = "CHEEZBURGER"
 *     logcat { "I CAN HAZ $state?" }
 *     // logcat output: D/MouseController: I CAN HAZ CHEEZBURGER?
 *
 *     logcat(INFO) { "DID U ASK 4 MOAR INFO?" }
 *     // logcat output: I/MouseController: DID U ASK 4 MOAR INFO?
 *
 *     logcat { exception.asLog() }
 *     // logcat output: D/MouseController: java.lang.RuntimeException: FYLEZ KERUPTED
 *     //                        at sample.MouseController.play(MouseController.kt:22)
 *     //                        ...
 *
 *     logcat("Lolcat") { "OH HI" }
 *     // logcat output: D/Lolcat: OH HI
 *   }
 * }
 * ```
 *
 * To install a logger, see [LogcatLogger].
 */
inline fun Any.logcat(
   priority: LogPriority = LogPriority.DEBUG,
   /**
    * If provided, the log will use this tag instead of the simple class name of `this` at the call
    * site.
    */
   tag: String? = null,
   message: () -> String
) {
   LogcatLogger.logger.let { logger ->
      if (logger.isLoggable(priority)) {
         val tagOrCaller = tag ?: outerClassSimpleNameInternalOnlyDoNotUseKThxBye()
         logger.log(priority, tagOrCaller, message())
      }
   }
}

/**
 * An overload for logging that does not capture the calling code as tag. This should only
 * be used in standalone functions where there is no `this`.
 * @see logcat above
 */
inline fun logcat(
   tag: String,
   priority: LogPriority = LogPriority.DEBUG,
   message: () -> String
) {
   with(LogcatLogger.logger) {
      if (isLoggable(priority)) {
         log(priority, tag, message())
      }
   }
}

@PublishedApi
internal fun Any.outerClassSimpleNameInternalOnlyDoNotUseKThxBye(): String {
   val javaClass = this::class.java
   val fullClassName = javaClass.name
   val outerClassName = fullClassName.substringBefore('$')
   val simplerOuterClassName = outerClassName.substringAfterLast('.')
   return if (simplerOuterClassName.isEmpty()) {
      fullClassName
   } else {
      simplerOuterClassName.removeSuffix("Kt")
   }
}
