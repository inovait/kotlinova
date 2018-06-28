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