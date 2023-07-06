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

package si.inova.kotlinova.core.test.outcomes

import io.kotest.assertions.Actual
import io.kotest.assertions.Expected
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.collectOrThrow
import io.kotest.assertions.errorCollector
import io.kotest.assertions.intellijFormatError
import io.kotest.assertions.print.Printed
import io.kotest.assertions.withClue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeInstanceOf
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome

infix fun <T> Outcome<T>.shouldBeSuccessWithData(expectedData: T) {
   assertSoftly {
      this should beInstanceOf<Outcome.Success<T>>()

      if (this is Outcome.Success) {
         this
            .data
            .let {
               withClue("Outcome's data does not match") {
                  it.shouldBe(expectedData)
               }
            }
      } else if (this is Outcome.Error) {
         reportContainingError()
      }
      Unit
   }
}

infix fun <T> Outcome<T>.shouldBeProgressWithData(expectedData: T?) {
   assertSoftly {
      this should beInstanceOf<Outcome.Progress<T>>()

      if (this is Outcome.Progress) {
         this
            .data
            .let {
               withClue("Outcome's data does not match") {
                  it.shouldBe(expectedData)
               }
            }
      } else if (this is Outcome.Error) {
         reportContainingError()
      }
      Unit
   }
}

fun <T> Outcome<T>.shouldBeProgressWith(
   expectedData: T? = data,
   expectedProgress: Float? = null
) {
   assertSoftly {
      this should beInstanceOf<Outcome.Progress<T>>()

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
      } else if (this is Outcome.Error) {
         reportContainingError()
      }
      Unit
   }
}

fun <T> Outcome<T>.shouldBeErrorWith(
   expectedData: T? = data,
   exceptionType: Class<out CauseException>? = null,
   exceptionMessage: String? = null
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
                        throw AssertionError("Exception's message: ${intellijFormatError(expected, actual)}", exception)
                     }
                  }
            }

            if (exceptionType != null) {
               javaClass
                  .let {
                     if (exception.javaClass != exceptionType) {
                        val expected = Expected(Printed(exceptionType.name))
                        val actual = Actual(Printed(it.name))
                        throw AssertionError("Exception type: ${intellijFormatError(expected, actual)}", exception)
                     }
                  }
            }
         }
   }

   return returnException
}

private fun Outcome.Error<*>.reportContainingError() {
   errorCollector.collectOrThrow(AssertionError("Reported error: $exception", exception))
}
