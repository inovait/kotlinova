@file:JvmName("AssertUtils")

package si.inova.kotlinova.testing.asserts

import org.assertj.core.api.AbstractAssert

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