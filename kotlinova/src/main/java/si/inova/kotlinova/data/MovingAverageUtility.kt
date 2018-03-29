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
