/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

@file:JvmName("AssertUtils")

package si.inova.kotlinova.testing.asserts

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Condition

/**
 * Variant of [AbstractAssert.matches] with flipped parameters to move lambda
 * to last parameter
 */
fun <S : AbstractAssert<S, T>, T> AbstractAssert<S, T>.matches(
    description: String,
    predicate: (T) -> Boolean
): S {
    return matches(predicate, description)
}

fun <T> matching(description: String? = null, block: (T) -> Boolean): Condition<T> {
    return object : Condition<T>(description) {
        override fun matches(value: T): Boolean {
            return block(value)
        }
    }
}

/**
 * Extract value from assert. This should only be used to implement custom assertion, not with
 * actual tests.
 */
val <T> AbstractAssert<*, T>.actualValue: T
    get() {
        var value: T? = null

        satisfies {
            value = it
        }

        return value!!
    }

/**
 * Wait for specific test condition to be true. This test method makes the assertion periodically
 * until it becomes true or until timeout hits.
 *
 * If you are running instrumented tests, it is generally recommended
 * to use [IdlingResource] instead if possible
 */
fun waitForMatch(timeoutMs: Long = 5000, periodMs: Long = 10, condition: () -> Unit) {
    val end = System.currentTimeMillis() + timeoutMs

    while (true) {
        try {
            condition()
            break
        } catch (e: AssertionError) {
            if (System.currentTimeMillis() >= end) {
                throw e
            }

            Thread.sleep(periodMs)
        }
    }
}