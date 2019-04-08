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