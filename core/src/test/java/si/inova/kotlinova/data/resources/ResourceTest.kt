package si.inova.kotlinova.data.resources

import org.junit.Assert.assertEquals
import org.junit.Test

class ResourceTest {
    @Test
    fun unwrapValue() {
        val resource = Resource.Success(1337)

        assertEquals(1337, resource.unwrap())
    }

    @Test(expected = CloneNotSupportedException::class)
    fun unwrapException() {
        val resource = Resource.Error<Int>(CloneNotSupportedException())
        resource.unwrap()
    }

    @Test(expected = IllegalStateException::class)
    fun unwrapUnsupported() {
        val resource = Resource.Loading<Int>()
        resource.unwrap()
    }
}