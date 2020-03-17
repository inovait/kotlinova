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