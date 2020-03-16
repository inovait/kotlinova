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

package si.inova.kotlinova.math

/**
 * Structure that represents 3D vector
 *
 * @author Simon Korošec, adapted by Matej Drobnic
 */
data class Vector(val x: Double, val y: Double, val z: Double) {
    fun magnitude(): Double {
        return Math.sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vector {
        val mag = magnitude()
        return Vector(x / mag, y / mag, z / mag)
    }

    operator fun plus(vec2: Vector): Vector {
        return Vector(x + vec2.x, y + vec2.y, z + vec2.z)
    }

    fun scalarProduct(vec2: Vector): Double {
        return x * vec2.x + y * vec2.y + z * vec2.z
    }

    /**
     * Calculates the angle in radians between two vectors
     */
    fun angleBetweenVectors(vec2: Vector): Double {
        val magA = magnitude()
        val magB = vec2.magnitude()

        if (magA == 0.0 || magB == 0.0) {
            return 0.0
        }

        val cosAlpha = this.scalarProduct(vec2) / (magA * magB)

        return Math.acos(cosAlpha)
    }

    internal fun projectOntoPlane(planeNormal: Vector): Vector {
        val dot = this.scalarProduct(planeNormal)
        val dotN = Vector(
                planeNormal.x * dot.toInt(), planeNormal.y * dot.toInt(),
                planeNormal.z * dot.toInt()
        )
        return this - dotN
    }

    operator fun minus(vec2: Vector): Vector {
        return Vector(x - vec2.x, y - vec2.y, z - vec2.z)
    }
}
