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

package si.inova.kotlinova.utils

import androidx.fragment.app.Fragment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.DummyFragmentActivity

class FragmentManagerTest {
    @get:Rule
    val activityRule = ActivityTestRule(DummyFragmentActivity::class.java, true, true)

    @Test
    fun testIsInBackStackMethod() {
        val activity = activityRule.activity
        val fragmentManager = activity.supportFragmentManager

        val firstFragment = TestFragment()
        val secondFragment = BackStackFragment()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activity.swapFragment(firstFragment)
            activity.swapFragment(secondFragment)
        }

        assertEquals(true, fragmentManager.isInBackStack(TestFragment::class.java))

        assertEquals(true, fragmentManager.isInBackStack(BackStackFragment::class.java))

        assertEquals(false, fragmentManager.isInBackStack(NotBackStackFragment::class.java))

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            fragmentManager.popBackStackImmediate()
            assertEquals(false, fragmentManager.isInBackStack(NotBackStackFragment::class.java))
            fragmentManager.popBackStackImmediate()
            assertEquals(false, fragmentManager.isInBackStack(BackStackFragment::class.java))
            fragmentManager.popBackStackImmediate()
            assertEquals(false, fragmentManager.isInBackStack(TestFragment::class.java))
        }
    }

    class TestFragment : Fragment()

    class BackStackFragment : Fragment()

    class NotBackStackFragment : Fragment()
}