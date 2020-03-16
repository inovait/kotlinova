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

package si.inova.kotlinova.testing.fakes

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat

class TestCallable<T> : (T) -> Unit {
    var called = false
        private set
    var lastResult: T? = null
        private set

    override fun invoke(result: T) {
        called = true
        lastResult = result
    }

    val suspendCallback: suspend (T) -> Unit = {
        invoke(it)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T, S : AbstractAssert<S, TestCallable<T>>?>
    AbstractAssert<S, TestCallable<T>>.wasCalled(): S {
    satisfies {
        assertThat(it.called)
            .describedAs("Callable should be called, but was not")
            .isTrue()
    }

    return this as S
}

@Suppress("UNCHECKED_CAST")
fun <T, S : AbstractAssert<S, TestCallable<T>>?>
    AbstractAssert<S, TestCallable<T>>.wasNotCalled(): S {
    satisfies {
        assertThat(it.called)
            .describedAs("Callable should not be called, but it was")
            .isFalse()
    }

    return this as S
}

@Suppress("UNCHECKED_CAST")
fun <T, S : AbstractAssert<S, TestCallable<T>>?>
    AbstractAssert<S, TestCallable<T>>.wasCalledWith(expected: T): S {
    satisfies {
        assertThat(it.called)
            .describedAs("Callable should be called, but was not")
            .isTrue()

        assertThat(it.lastResult)
            .describedAs("Callable was not called with specified value")
            .isEqualTo(expected)
    }

    return this as S
}