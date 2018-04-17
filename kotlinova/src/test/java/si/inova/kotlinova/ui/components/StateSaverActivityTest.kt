package si.inova.kotlinova.ui.components

import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import si.inova.kotlinova.ui.state.StateSavedProperty
import si.inova.kotlinova.ui.state.StateSaver
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
class StateSaverActivityTest {
    @Test
    fun testStateSaving() {
        val activity = Robolectric.buildActivity(TestActivity::class.java).create()
        activity.get().testInt = 20
        assertFalse(activity.get().set)
        activity.get().testString = "TeST"
        assertTrue(activity.get().set)

        assertEquals("NONDEFINED", activity.get().customStateSaver.value)
        activity.get().customStateSaver.value = "TestValue"

        val outState = Bundle()
        activity.saveInstanceState(outState)
        activity.destroy()

        val recreatedActivity = Robolectric.buildActivity(TestActivity::class.java)
            .create(outState)

        assertEquals(20, recreatedActivity.get().testInt)
        assertEquals("TeST", recreatedActivity.get().testString)
        assertEquals("TestValue", activity.get().customStateSaver.value)
        assertFalse(recreatedActivity.get().set)
    }

    class TestActivity : StateSaverActivity() {
        var testInt by StateSavedProperty<Int>(0)
        var testString by StateSavedProperty<String>("") {
            set = true
        }

        var set = false

        val customStateSaver = CustomStateSaver(this)
    }

    class CustomStateSaver(stateSavingComponent: StateSavingComponent) : StateSaver<String>() {
        var value = "NONDEFINED"

        init {
            register(stateSavingComponent.stateSaverManager, "CustomStateSaver")
        }

        override fun saveState(): String? {
            return value
        }

        override fun loadState(savedValue: String?) {
            if (savedValue != null) {
                value = savedValue
            }
        }
    }
}