package si.inova.kotlinova.ui.arguments

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import si.inova.kotlinova.testing.DummyFragmentActivity

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
class FragmentArgumentTest {
    @Test
    fun testArguments() {
        val activity = Robolectric.buildActivity(DummyFragmentActivity::class.java).create()
        val fragment = TestFragment().apply {
            twenty = 20
            testString = "TeST"

            intNullArgument = null
            secondIntNullArgument = 35
        }

        activity
            .get()
            .supportFragmentManager.beginTransaction()
            .replace(1, fragment)
            .commitNow()

        val outState = Bundle()
        activity.saveInstanceState(outState)
        activity.destroy()

        val secondActivity = Robolectric.buildActivity(DummyFragmentActivity::class.java)
            .create(outState)

        val recreatedFragment = secondActivity
            .get()
            .supportFragmentManager
            .findFragmentById(1) as TestFragment

        assertEquals(20, recreatedFragment.twenty)
        assertEquals("TeST", recreatedFragment.testString)
        assertEquals(null, recreatedFragment.intNullArgument)
        assertEquals(35, recreatedFragment.secondIntNullArgument)
    }

    @Test
    fun testArgumentsSetImmediately() {
        val activity = Robolectric.buildActivity(DummyFragmentActivity::class.java).create()
        val fragment = TestFragment().apply {
            twentyWhichLaterShouldBeForty = 20
            testString = "TeST"

            intNullArgument = null
            secondIntNullArgument = 35
        }

        val outState = Bundle()
        activity.apply {
            get().supportFragmentManager.beginTransaction().replace(1, fragment).commitNow()
            saveInstanceState(outState)
            destroy()
        }

        val secondActivity =
            Robolectric.buildActivity(DummyFragmentActivity::class.java).create(outState).get()

        val recreatedFragment =
            secondActivity.supportFragmentManager.findFragmentById(1) as TestFragment

        recreatedFragment.twentyWhichLaterShouldBeForty = 40

        assertEquals(40, recreatedFragment.twentyWhichLaterShouldBeForty)
        assertEquals("TeST", recreatedFragment.testString)
        assertEquals(null, recreatedFragment.intNullArgument)
        assertEquals(35, recreatedFragment.secondIntNullArgument)
    }

    class TestFragment : Fragment() {
        var twenty by FragmentArgument<Int>()
        var twentyWhichLaterShouldBeForty by FragmentArgument<Int>()
        var testString by FragmentArgument<String>()

        var intNullArgument by NullableFragmentArgument<Int>()
        var secondIntNullArgument by NullableFragmentArgument<Int>()
    }
}