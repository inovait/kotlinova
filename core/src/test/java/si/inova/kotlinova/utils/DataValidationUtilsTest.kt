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

package si.inova.kotlinova.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
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

        val castList: List<Int> = castToList(list)!!
        assertEquals(list, castList)
    }

    @Test
    fun castToListNullTest() {
        assertNull(castToList<String>(null))
    }

    @Test(expected = InvalidDataFormatException::class)
    fun checkNullAndSafeCastWhenNull() {
        checkNullAndSafeCast<String>(null, "")
    }

    @Test(expected = InvalidDataFormatException::class)
    fun checkNullAndSafeCastWhenInvalidType() {
        val input: Any = 10

        checkNullAndSafeCast<String>(input, "")
    }

    @Test
    fun checkNullAndSafeCastWhenOk() {
        val input: Any = "10"

        assertEquals("10", checkNullAndSafeCast<String>(input, ""))
    }

    @Test
    fun safeCastNullWhenNull() {
        assertNull(safeCastNull<String>(null, ""))
    }

    @Test(expected = InvalidDataFormatException::class)
    fun safeCastNullWhenInvalidType() {
        val input: Any = 10

        safeCastNull<String>(input, "")
    }

    @Test
    fun safeCastNullWhenOk() {
        val input: Any = "10"

        assertEquals("10", safeCastNull<String>(input, ""))
    }
}