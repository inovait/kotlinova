@file:JvmName("ListUtils")

package si.inova.kotlinova.utils

import java.util.Random

private val RANDOM by lazy { Random() }
/**
 * @author Matej Drobnic
 */

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