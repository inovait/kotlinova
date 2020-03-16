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

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author Matej Drobnic
 */
class VectorTest {
    @Test
    @Throws(Exception::class)
    fun magnitude() {
        var vector = Vector(-1280.0, 832.0, 576.0)
        assertEquals(1631.686244, vector.magnitude(), 0.000001)
        vector = Vector(-960.0, 1024.0, 512.0)
        assertEquals(1494.095044, vector.magnitude(), 0.000001)
        vector = Vector(-1152.0, 896.0, -256.0)
        assertEquals(1481.707124, vector.magnitude(), 0.000001)
    }

    @Test
    fun angleBetweenVectors() {
        var vectorA = Vector(-896.0, 128.0, 64.0)
        var vectorB = Vector(-1280.0, 832.0, 576.0)
        assertEquals(29.36928675, Math.toDegrees(vectorA.angleBetweenVectors(vectorB)), 0.000001)

        vectorA = Vector(-1024.0, 0.0, 0.0)
        vectorB = Vector(-896.0, 128.0, 64.0)
        assertEquals(9.074585886, Math.toDegrees(vectorA.angleBetweenVectors(vectorB)), 0.000001)
    }
}