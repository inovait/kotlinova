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

@file:JvmName("ResourceAsserts")

package si.inova.kotlinova.testing.asserts

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.value

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isSuccess(): ObjectAssert<Resource.Success<T>> {
    satisfies { input ->
        if (input is Resource.Error<*>) {
            throw AssertionError(
                "Result should be Resource.Success, but is Error",
                input.exception
            )
        }
    }

    isInstanceOf(Resource.Success::class.java)
    return assertThat(this.actualValue as Resource.Success<T>)
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.assertThatValue(): ObjectAssert<T?> {
    @Suppress("UNCHECKED_CAST")
    return assertThat(this.actualValue.value)
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isLoading(): ObjectAssert<Resource.Loading<T>> {
    satisfies { input ->
        if (input is Resource.Error<*>) {
            throw AssertionError(
                "Result should be Resource.Success, but is Error",
                input.exception
            )
        }
    }

    isInstanceOf(Resource.Loading::class.java)
    return assertThat(this.actualValue as Resource.Loading<T>)
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isSuccessWithValue(value: T): S {
    isSuccess()

    return satisfies {
        @Suppress("UNCHECKED_CAST")
        it as Resource.Success<T>

        assertThat(it.data).describedAs("Success Resource's data").isEqualTo(value)
    }
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isLoadingWithValue(value: T): S {
    isLoading()

    return satisfies {
        @Suppress("UNCHECKED_CAST")
        it as Resource.Loading<T>

        assertThat(it.data).describedAs("Loading Resource's data").isEqualTo(value)
    }
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isErrorSatisfying(requirements: (Throwable) -> Unit): S {
    return satisfies { input ->
        isInstanceOf(Resource.Error::class.java)
        input as Resource.Error<*>

        requirements(input.exception)
    }
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isError(errorClass: Class<out Throwable>): S {
    return isErrorSatisfying {
        assertThat(it).isInstanceOf(errorClass)
    }
}