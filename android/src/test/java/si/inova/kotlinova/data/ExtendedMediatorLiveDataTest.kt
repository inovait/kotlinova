package si.inova.kotlinova.data

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * @author Matej Drobnic
 */
class ExtendedMediatorLiveDataTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun testMemory() {
        val exMediatorLiveData = ExtendedMediatorLiveData<Int>()
        val target = MutableLiveData<Int>()

        exMediatorLiveData.observeForever {}

        target.value = 10
        assertNull(exMediatorLiveData.value)

        exMediatorLiveData.addSource(target) {
            exMediatorLiveData.value = it
        }

        assertTrue(target.hasActiveObservers())
        assertEquals(10, exMediatorLiveData.value)

        target.value = 20
        assertEquals(20, exMediatorLiveData.value)

        exMediatorLiveData.removeSource(target)
        target.value = 30
        assertFalse(target.hasActiveObservers())
        assertEquals(20, exMediatorLiveData.value)

        val target2 = MutableLiveData<Int>()
        exMediatorLiveData.addSource(target2) {
            exMediatorLiveData.value = it
        }
        assertTrue(target2.hasActiveObservers())
        assertEquals(20, exMediatorLiveData.value)

        target2.value = 40
        assertEquals(40, exMediatorLiveData.value)

        exMediatorLiveData.removeAllSources()
        target2.value = 50
        assertFalse(target2.hasActiveObservers())
        assertEquals(40, exMediatorLiveData.value)
    }
}