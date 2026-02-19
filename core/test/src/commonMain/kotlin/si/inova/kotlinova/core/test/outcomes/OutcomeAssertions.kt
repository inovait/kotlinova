/*
 * Copyright 2026 INOVA IT d.o.o.
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

package si.inova.kotlinova.core.test.outcomes

import io.kotest.assertions.Actual
import io.kotest.assertions.Expected
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.print.Printed
import io.kotest.assertions.withClue
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.LoadingStyle
import si.inova.kotlinova.core.outcome.Outcome
import kotlin.reflect.KClass

infix fun <T> Outcome<T>.shouldBeSuccessWithData(expectedData: T) {
   assertSoftly {
      this should beInstanceOfOutcome<Outcome.Success<T>>()

      if (this is Outcome.Success) {
         this
            .data
            .let {
               withClue("Outcome's data does not match") {
                  it.shouldBe(expectedData)
               }
            }
      }
      Unit
   }
}

infix fun <T> Outcome<T>.shouldBeProgressWithData(expectedData: T?) {
   assertSoftly {
      this should beInstanceOfOutcome<Outcome.Progress<T>>()

      if (this is Outcome.Progress) {
         this
            .data
            .let {
               withClue("Outcome's data does not match") {
                  it.shouldBe(expectedData)
               }
            }
      }
      Unit
   }
}

fun <T> Outcome<T>.shouldBeProgressWith(
   expectedData: T? = data,
   expectedProgress: Float? = null,
   expectedStyle: LoadingStyle? = null,
) {
   assertSoftly {
      this should beInstanceOfOutcome<Outcome.Progress<T>>()

      if (this is Outcome.Progress) {
         this
            .data
            .let {
               withClue("Outcome's data does not match") {
                  it.shouldBe(expectedData)
               }
            }
            .apply {
               if (expectedProgress != null) {
                  progress
                     .let {
                        withClue("Outcome's progress does not match") {
                           it.shouldBe(expectedProgress)
                        }
                     }
               }
            }
            .apply {
               if (expectedStyle != null) {
                  style
                     .let {
                        withClue("Outcome's style does not match") {
                           it.shouldBe(expectedStyle)
                        }
                     }
               }
            }
      }
      Unit
   }
}

@Suppress("CognitiveComplexMethod") // Just a bunch of successive type checks
fun <T> Outcome<T>.shouldBeErrorWith(
   expectedData: T? = data,
   exceptionType: KClass<out CauseException>? = null,
   exceptionMessage: String? = null,
): Exception {
   lateinit var returnException: Exception

   assertSoftly {
      this
         .shouldBeInstanceOf<Outcome.Error<T>>()
         .apply {
            data
               .let {
                  withClue("Outcome's data does not match") {
                     it.shouldBe(expectedData)
                  }
               }
         }
         .exception
         .apply {
            returnException = exception

            if (exceptionMessage != null) {
               message
                  .let {
                     if (it != exceptionMessage) {
                        val expected = Expected(Printed(exceptionMessage))
                        val actual = Actual(Printed(it ?: "null"))
                        throw AssertionError("Exception's message: ${intellijFormattedComparison(expected, actual)}", exception)
                     }
                  }
            }

            if (exceptionType != null) {
               this::class
                  .let {
                     if (exception::class != exceptionType) {
                        val expected = Expected(Printed(exceptionType::class.simpleName.orEmpty()))
                        val actual = Actual(Printed(it::class.simpleName.orEmpty()))
                        throw AssertionError("Exception type: ${intellijFormattedComparison(expected, actual)}", exception)
                     }
                  }
            }
         }
   }

   return returnException
}

private inline fun <reified T : Outcome<*>> beInstanceOfOutcome() = object : Matcher<Outcome<*>> {
   override fun test(value: Outcome<*>): MatcherResult {
      return MatcherResult(
         value is T,
         {
            if (value is Outcome.Error) {
               "Outcome should be a ${T::class.simpleName ?: "NULL"} but was an Outcome.Error " +
                  "with exception ${value.exception.stackTraceToString()}"
            } else {
               "Outcome should be a ${T::class.simpleName ?: "NULL"} but was a ${value::class.simpleName.orEmpty()}."
            }
         },
         { "Outcome should not be a ${T::class.simpleName ?: "NULL"} but it was." }
      )
   }
}

// Copied from https://github.com/kotest/kotest/blob/04f888183b35a3c834000efc673c9410b2d39c87/kotest-assertions/kotest-assertions-shared/src/jvmMain/kotlin/io/kotest/assertions/AssertionErrorBuilder.jvm.kt#L59
private fun intellijFormattedComparison(expected: Expected, actual: Actual): String {
   // only include types if they are different and neither is null
   val includeTypes = when {
      expected.value.type == null || actual.value.type == null -> false
      expected.value.type != actual.value.type -> true
      else -> false
   }

   fun format(printed: Printed): String {
      val qualifiedName = printed.type?.qualifiedName
      return if (includeTypes && qualifiedName != null) {
         "$qualifiedName<${printed.value}>"
      } else {
         "<${printed.value}>"
      }
   }
   return "expected:${format(expected.value)} but was:${format(actual.value)}"
}
