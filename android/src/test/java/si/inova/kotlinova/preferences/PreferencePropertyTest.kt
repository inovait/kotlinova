package si.inova.kotlinova.preferences

import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import si.inova.kotlinova.utils.put

/**
 * @author Matej Drobnic
 */
@Ignore
@RunWith(RobolectricTestRunner::class)
class PreferencePropertyTest {
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun init() {
        val context = RuntimeEnvironment.application.applicationContext
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        sharedPreferences.edit()
            .clear()
            .put("a", 10)
            .put("b", "Test")
            .apply()
    }

    @Test
    fun testGetAndDefaults() {
        val dataObject = object {
            var a by preference(sharedPreferences, 50)
            var b by preference(sharedPreferences, "NotTest")
            var c by preference(sharedPreferences, 99)
        }

        assertEquals(10, dataObject.a)
        assertEquals("Test", dataObject.b)
        assertEquals(99, dataObject.c)
    }

    @Test
    fun testSavingAndGetting() {
        val dataObject = object {
            var d by preference(sharedPreferences, -1)
        }

        dataObject.d = 86

        val secondDataObject = object {
            var d by preference(sharedPreferences, -1)
        }

        assertEquals(86, secondDataObject.d)
    }

    @Test
    fun testSavingAndGettingNull() {
        val dataObject = object {
            var d by preference<Int?>(sharedPreferences, -1)
        }

        dataObject.d = 86

        val secondDataObject = object {
            var d by preference<Int?>(sharedPreferences, -1)
        }

        assertEquals(86, secondDataObject.d)
        secondDataObject.d = null

        val thirdDataObject = object {
            var d by preference<Int?>(sharedPreferences, -1)
        }

        assertEquals(-1, thirdDataObject.d)
    }

    @Test
    fun testNullDefault() {
        val dataObject = object {
            var e by preference<String?>(sharedPreferences, null)
        }

        assertNull(dataObject.e)

        dataObject.e = "abc"

        val secondDataObject = object {
            var e by preference<String?>(sharedPreferences, null)
        }

        assertEquals("abc", secondDataObject.e)
    }

    @Test
    fun testReturnNullWhenDefaultNumberIsNullAndPropertyNotPresent() {
        val dataObject = object {
            var e by preference<Int?>(sharedPreferences, null)
        }

        assertNull(dataObject.e)
    }

    @Test
    fun testReturnNumberWhenDefaultNumberIsNullAndPropertyPresent() {
        val dataObject = object {
            var a by preference<Int?>(sharedPreferences, null)
        }

        assertEquals(10, dataObject.a)
    }
}