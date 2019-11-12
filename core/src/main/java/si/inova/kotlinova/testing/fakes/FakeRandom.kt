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

import java.util.LinkedList
import java.util.Queue
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