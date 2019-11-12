/*
 * Copyright © 2016, Connected Travel, LLC – All Rights Reserved.
 *
 * All information contained herein is property of Connected Travel, LLC including, but
 * not limited to, technical and intellectual concepts which may be embodied within.
 *
 * Dissemination or reproduction of this material is strictly forbidden unless prior written
 * permission, via license, is obtained from Connected Travel, LLC. If permission is obtained,
 * this notice, and any other such legal notices, must remain unaltered.
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