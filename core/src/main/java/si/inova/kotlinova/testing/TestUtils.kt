/**
 * @author Matej Drobnic
 */
@file:JvmName("TestUtils")

package si.inova.kotlinova.testing

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.createInstance
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.mockito.ArgumentMatcher
import org.mockito.Mockito

infix fun <T> Comparable<T>.isGreaterThan(other: T) {
    assertTrue("$this is greater than $other", this > other)
}

infix fun <T> Comparable<T>.isEqualTo(other: T) {
    assertTrue("$this is equal than $other", this.compareTo(other) == 0)
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

