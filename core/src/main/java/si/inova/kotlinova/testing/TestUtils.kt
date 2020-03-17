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

/**
 * @author Matej Drobnic
 */
@file:JvmName("TestUtils")

package si.inova.kotlinova.testing

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.internal.createInstance
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import si.inova.kotlinova.data.resources.Resource
import javax.inject.Provider

infix fun <T> Comparable<T>.isGreaterThan(other: T) {
    assertTrue("$this is greater than $other", this > other)
}

infix fun <T> Comparable<T>.isEqualTo(other: T) {
    assertTrue("$this is equal than $other", this.compareTo(other) == 0)
}

fun <T> T.provider(): Provider<T> {
    return Provider { this }
}

/**
 * Variant of Mockito's [argThat] that provides better error printing for easier debugging.
 *
 * @param block Code where you specify pair of objects (*modified argument to check*) to check for inequality.
 */
inline fun <reified T : Any> paramEquals(crossinline block: (T) -> Pair<T?, T?>): T {
    val matcher = ArgumentMatcher<T> { argument ->
        val pair = block(argument)
        Assert.assertEquals(pair.second, pair.first)
        true
    }

    return Mockito.argThat(matcher) ?: createInstance(T::class)
}

fun <T, R : Any> assertIs(test: R?, expectedClass: Class<T>) {
    assertTrue(
        "Object should be ${expectedClass.name}, but is ${test?.javaClass?.name}",
        test != null && expectedClass.isAssignableFrom(test.javaClass)
    )
}

inline fun <reified T> assertIs(test: Any?) {
    si.inova.kotlinova.testing.assertIs(test, T::class.java)
}

fun <T> assertIsSuccess(input: Resource<T>?) {
    if (input is Resource.Error) {
        throw AssertionError(
            "Result should be Resource.Success, but is Error",
            input.exception
        )
    }

    assertIs<Resource.Success<T>>(input)
}