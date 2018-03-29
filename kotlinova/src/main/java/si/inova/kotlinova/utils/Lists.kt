@file:JvmName("ListUtils")

package si.inova.kotlinova.utils

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