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

package si.inova.kotlinova.data

import java.util.ArrayDeque

/**
 * Calculates a moving average over a set of values
 *
 * @author Domen Mori, adapted by Matej Drobnic
 */
class MovingAverageUtility(private var maSize: Int) {
    private val maValues = ArrayDeque<Double>()
    private var maVal: Double = 0.toDouble()

    fun clear() {
        maVal = 0.0
        maValues.clear()
    }

    fun resize(size: Int) {
        maSize = size
        while (maValues.size > maSize) {
            maValues.poll()
        }
    }

    fun add(force: Double) {
        maValues.add(force)
        val maNumSamples = maValues.size
        if (maNumSamples > maSize) {
            maVal -= maValues.poll() / maSize
            maVal += force / maSize
        } else {
            maVal = (maVal * (maNumSamples - 1) + force) / maNumSamples
        }
    }

    operator fun plusAssign(force: Double) {
        add(force)
    }

    fun get(): Double {
        return maVal
    }
}
