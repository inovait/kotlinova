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
