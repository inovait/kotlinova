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

import org.junit.Assert
import org.junit.Test

/**
 * @author Domen Mori, adapted by Matej Drobnic
 */
class MovingAverageUtilityTest {
    @Test
    fun testSingleValue() {
        val movingAverageUtility = MovingAverageUtility(10)
        movingAverageUtility += 5.0
        Assert.assertEquals(5.0, movingAverageUtility.get(), 0.0001)
    }

    @Test
    fun testMoreValues() {
        val movingAverageUtility = MovingAverageUtility(8)
        movingAverageUtility.add(6.0)
        movingAverageUtility.add(7.0)
        movingAverageUtility.add(5.0)
        movingAverageUtility.add(8.0)
        movingAverageUtility.add(9.0)
        Assert.assertEquals(7.0, movingAverageUtility.get(), 0.0001)
    }

    @Test
    fun testWithMoreThanCapacity() {
        val howManyItems = 7
        val movingAverageUtility = MovingAverageUtility(howManyItems)
        val array = doubleArrayOf(
                1.0,
                5.5,
                4.0,
                7.2,
                11.99,
                1.0,
                8.0,
                4.0,
                2.0,
                6.0,
                12.0,
                17.11,
                6.88,
                2.18,
                7.11,
                21.1181,
                -2.117,
                6.1774,
                2.9855
        )
        for (vall in array) {
            movingAverageUtility.add(vall)
        }
        var compareVal = 0.0
        var i = array.size - 1
        var guard = 0
        while (guard < howManyItems) {
            compareVal += array[i]
            --i
            ++guard
        }
        compareVal /= howManyItems.toDouble()
        Assert.assertEquals(compareVal, movingAverageUtility.get(), 0.0001)
    }
}