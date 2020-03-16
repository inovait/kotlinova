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