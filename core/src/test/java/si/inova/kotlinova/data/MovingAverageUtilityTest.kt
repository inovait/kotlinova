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