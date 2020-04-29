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

/**
 * Returns *true* if collection contains only selected element and no others
 */
fun <T> List<T>.containsOnly(element: T): Boolean {
    return size == 1 && first() == element
}
