package si.inova.kotlinova.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import si.inova.kotlinova.exceptions.InvalidDataFormatException

/**
 * @author Matej Drobnic
 */
class DataValidationUtilsTest {
    @Test
    fun validateDoNotWrapInvalidDataFormatException() {
        val expectedException = InvalidDataFormatException("")
        try {
            validate {
                throw expectedException
            }
        } catch (e: InvalidDataFormatException) {
            assertSame(expectedException, e)
        }
    }

    @Test
    fun validateWrapException() {
        val expectedException = RuntimeException("")

        try {
            validate {
                throw expectedException
            }
        } catch (e: InvalidDataFormatException) {
            assertNotSame(expectedException, e)
            assertSame(expectedException, e.cause)
        }
    }

    @Test
    fun validateReturnValue() {
        val expectedValue = Any()

        val returnedValue = validate {
            return@validate expectedValue
        }

        assertSame(expectedValue, returnedValue)
    }

    @Test(expected = ClassCastException::class)
    fun castToListFail() {
        val list = listOf(1, 2, 3)

        castToList<String>(list)
    }

    @Test
    fun castToListSuccess() {
        val list: Any = listOf(1, 2, 3)

        val castList: List<Int> = castToList(list)
        assertEquals(list, castList)
    }
}