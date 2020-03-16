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

import java.util.*
import kotlin.random.Random

class FakeRandom : Random() {
    var nextNumbers: Queue<Long>? = null

    fun setNextRandomNumber(number: Long) {
        nextNumbers = LinkedList(listOf(number))
    }

    fun setNextRandomNumber(number: Int) {
        nextNumbers = LinkedList(listOf(number.toLong()))
    }

    override fun nextBits(bitCount: Int): Int {
        throw UnsupportedOperationException("Method not faked")
    }

    override fun nextBoolean(): Boolean {
        throw UnsupportedOperationException("Method not faked")
    }

    override fun nextBytes(array: ByteArray): ByteArray {
        throw UnsupportedOperationException("Method not faked")
    }

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        throw UnsupportedOperationException("Method not faked")
    }

    override fun nextBytes(size: Int): ByteArray {
        throw UnsupportedOperationException("Method not faked")
    }

    override fun nextDouble(): Double {
        throw UnsupportedOperationException("Method not faked")
    }

    override fun nextDouble(until: Double): Double {
        throw UnsupportedOperationException("Method not faked")
    }

    override fun nextDouble(from: Double, until: Double): Double {
        throw UnsupportedOperationException("Method not faked")
    }

    override fun nextFloat(): Float {
        throw UnsupportedOperationException("Method not faked")
    }

    override fun nextInt(): Int {
        return nextNumbers?.poll()?.toInt() ?: error("Fake numbers not provided")
    }

    override fun nextInt(until: Int): Int {
        return nextNumbers?.poll()?.toInt() ?: error("Fake numbers not provided")
    }

    override fun nextInt(from: Int, until: Int): Int {
        return nextNumbers?.poll()?.toInt() ?: error("Fake numbers not provided")
    }

    override fun nextLong(): Long {
        return nextNumbers?.poll() ?: error("Fake numbers not provided")
    }

    override fun nextLong(until: Long): Long {
        return nextNumbers?.poll() ?: error("Fake numbers not provided")
    }

    override fun nextLong(from: Long, until: Long): Long {
        return nextNumbers?.poll() ?: error("Fake numbers not provided")
    }
}