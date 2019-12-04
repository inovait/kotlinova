package si.inova.kotlinova.ui.components

import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.android.R
import si.inova.kotlinova.testing.DummyFragmentActivity
import si.inova.kotlinova.ui.state.StateSavedProperty

class StateSaverFragmentTest {
    @get:Rule
    val activityRule = ActivityTestRule(DummyFragmentActivity::class.java, true, true)

    @Test
    fun testStateSaving() {
        val fragment = TestFragment()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activityRule.activity.swapFragment(fragment)
        }
        Espresso.onIdle()

        fragment.testInt = 20
        fragment.testString = "TeST"

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activityRule.activity.recreate()
        }
        Espresso.onIdle()

        val recreatedFragment = activityRule.activity
            .supportFragmentManager
            .findFragmentById(R.id.container) as TestFragment

        assertEquals(20, recreatedFragment.testInt)
        assertEquals("TeST", recreatedFragment.testString)
    }

    class TestFragment : StateSaverFragment() {
        var testInt by StateSavedProperty<Int>(0)
        var testString by StateSavedProperty<String>("")
    }
}