/**
 * @author Matej Drobnic
 */
@file:JvmName("ListUtils")

package si.inova.kotlinova.utils

import java.util.Random
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private val RANDOM by lazy { Random() }

/**
 * @return A shallow copy of the list
 */
fun <T> List<T>.copy(): List<T> {
    return ArrayList<T>(size).let {
        it.addAll(this)
        it
    }
}

/**
 * @return Random element of the list
 */
fun <E> List<E>.getRandomElement() = this[RANDOM.nextInt(this.size)]

/**
 * Returns the average value of the given function or `null` if there are no elements.
 */
inline fun <T> Collection<T>.meanBy(selector: (T) -> Int): Int? {
    var sum = 0

    val iterator = iterator()
    if (!iterator.hasNext()) return null

    while (iterator.hasNext()) {
        val e = iterator.next()
        val v = selector(e)
        sum += v
    }

    return sum / size
}

/**
 * Returns `true` if the collection is not null or empty.
 */
@UseExperimental(ExperimentalContracts::class)
fun <T> List<T>?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.isNotEmpty()
}