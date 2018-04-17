package si.inova.kotlinova.ui.components

import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import si.inova.kotlinova.testing.DummyFragmentActivity
import si.inova.kotlinova.ui.state.StateSavedProperty

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
class StateSaverFragmentTest {
    @Test
    fun testStateSaving() {
        val activity = Robolectric.buildActivity(DummyFragmentActivity::class.java).create()
        val fragment = TestFragment()

        activity
            .get()
            .supportFragmentManager.beginTransaction()
            .replace(1, fragment)
            .commitNow()

        fragment.testInt = 20
        fragment.testString = "TeST"

        val outState = Bundle()
        activity.saveInstanceState(outState)
        activity.destroy()

        val secondActivity = Robolectric.buildActivity(DummyFragmentActivity::class.java)
            .create(outState)

        val recreatedFragment = secondActivity
            .get()
            .supportFragmentManager
            .findFragmentById(1) as TestFragment

        assertEquals(20, recreatedFragment.testInt)
        assertEquals("TeST", recreatedFragment.testString)
    }

    class TestFragment : StateSaverFragment() {
        var testInt by StateSavedProperty<Int>(0)
        var testString by StateSavedProperty<String>("")
    }
}