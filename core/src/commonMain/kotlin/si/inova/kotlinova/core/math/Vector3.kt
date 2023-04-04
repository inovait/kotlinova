/*
 * Copyright 2023 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.kotlinova.core.math

/**
 * Structure that represents 3D vector
 */
data class Vector3(val x: Double, val y: Double, val z: Double)

fun Vector3.magnitude(): Double {
   return Math.sqrt(x * x + y * y + z * z)
}

fun Vector3.normalize(): Vector3 {
   val mag = magnitude()
   return Vector3(x / mag, y / mag, z / mag)
}

operator fun Vector3.plus(vec2: Vector3): Vector3 {
   return Vector3(x + vec2.x, y + vec2.y, z + vec2.z)
}

fun Vector3.scalarProduct(vec2: Vector3): Double {
   return x * vec2.x + y * vec2.y + z * vec2.z
}

/**
 * Calculates the angle in radians between two vectors
 */
fun Vector3.angleBetweenVectors(vec2: Vector3): Double {
   val magA = magnitude()
   val magB = vec2.magnitude()

   if (magA == 0.0 || magB == 0.0) {
      return 0.0
   }

   val cosAlpha = this.scalarProduct(vec2) / (magA * magB)

   return Math.acos(cosAlpha)
}

internal fun Vector3.projectOntoPlane(planeNormal: Vector3): Vector3 {
   val dot = this.scalarProduct(planeNormal)
   val dotN = Vector3(
      planeNormal.x * dot.toInt(),
      planeNormal.y * dot.toInt(),
      planeNormal.z * dot.toInt()
   )
   return this - dotN
}

operator fun Vector3.minus(vec2: Vector3): Vector3 {
   return Vector3(x - vec2.x, y - vec2.y, z - vec2.z)
}
