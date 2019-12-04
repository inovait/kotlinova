package si.inova.kotlinova.ui.components

import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.StateSavingTestActivity

/**
 * @author Matej Drobnic
 */
class StateSaverActivityTest {
    @get:Rule
    val rule = ActivityTestRule(StateSavingTestActivity::class.java, true, true)

    @Test
    fun restoreSavedDataAfterConfigurationChange() {
        val originalActivity = rule.activity

        originalActivity.testInt = 20
        assertFalse(originalActivity.set)
        originalActivity.testString = "TeST"
        assertTrue(originalActivity.set)

        assertEquals("NONDEFINED", originalActivity.customStateSaver.value)
        originalActivity.customStateSaver.value = "TestValue"

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            originalActivity.recreate()
        }
        Espresso.onIdle()

        val recreatedActivity = rule.activity

        assertEquals(20, recreatedActivity.testInt)
        assertEquals("TeST", recreatedActivity.testString)
        assertEquals("TestValue", originalActivity.customStateSaver.value)
        assertFalse(recreatedActivity.set)
    }
}