package si.inova.kotlinova.ui.arguments

import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.android.R
import si.inova.kotlinova.testing.DummyFragmentActivity

/**
 * @author Matej Drobnic
 */
class FragmentArgumentTest {
    @get:Rule
    val activityRule = ActivityTestRule(DummyFragmentActivity::class.java, true, true)

    @Test
    fun testRestoreArgumentsAfterConfigurationChange() {
        val fragment = TestFragment().apply {
            twenty = 20
            testString = "TeST"

            intNullArgument = null
            secondIntNullArgument = 35
        }

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activityRule.activity.swapFragment(fragment)
        }
        Espresso.onIdle()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activityRule.activity.recreate()
        }
        Espresso.onIdle()

        val recreatedFragment = activityRule.activity
            .supportFragmentManager
            .findFragmentById(R.id.container) as TestFragment

        assertEquals(20, recreatedFragment.twenty)
        assertEquals("TeST", recreatedFragment.testString)
        assertEquals(null, recreatedFragment.intNullArgument)
        assertEquals(35, recreatedFragment.secondIntNullArgument)
    }

    @Test
    fun testArgumentsSetImmediately() {
        val fragment = TestFragment().apply { twentyWhichLaterShouldBeForty = 20 }
        fragment.twentyWhichLaterShouldBeForty = 40
        assertEquals(40, fragment.twentyWhichLaterShouldBeForty)
    }

    class TestFragment : Fragment() {
        var twenty by FragmentArgument<Int>()
        var twentyWhichLaterShouldBeForty by FragmentArgument<Int>()
        var testString by FragmentArgument<String>()

        var intNullArgument by NullableFragmentArgument<Int>()
        var secondIntNullArgument by NullableFragmentArgument<Int>()
    }
}