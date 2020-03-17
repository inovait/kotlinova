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

package si.inova.kotlinova.ui.views

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.DummyFragmentActivity

/**
 * @author Matej Drobnic
 */
class FullScreenProgressBarTest {
    @get:Rule
    val activityRule = ActivityTestRule(DummyFragmentActivity::class.java, true, true)

    private lateinit var fullScreenProgressBar: FullScreenProgressBar

    @Before
    fun setUp() {
        val activity = activityRule.activity
        val fragment = HolderFragment()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activity.swapFragment(fragment)
        }
        Espresso.onIdle()

        fullScreenProgressBar = fragment.fullScreenProgressBar
    }

    @Test
    fun showAfterDelay() {
        fullScreenProgressBar.show()
        onView(withId(1337)).check(matches(not(isDisplayed())))

        SystemClock.sleep(100)
        onView(withId(1337)).check(matches(not(isDisplayed())))

        SystemClock.sleep(500)
        SystemClock.sleep(200) // Additional Wait for animation
        onView(withId(1337)).check(matches(isDisplayed()))
    }

    @Test
    fun hideAfterDelay() {
        fullScreenProgressBar.show()
        SystemClock.sleep(260)

        SystemClock.sleep(600)
        fullScreenProgressBar.hide()

        SystemClock.sleep(260) // Wait for animation
        onView(withId(1337)).check(matches(not(isDisplayed())))
    }

    @Test
    fun doNotHideImmediately() {
        fullScreenProgressBar.show()
        SystemClock.sleep(260)

        fullScreenProgressBar.hide()

        SystemClock.sleep(260) // Wait for potential animation
        onView(withId(1337)).check(matches(isDisplayed()))

        SystemClock.sleep(500)

        SystemClock.sleep(260) // Wait for animation
        onView(withId(1337)).check(matches(not(isDisplayed())))
    }

    @Test
    fun doNotShowAfterQuickHide() {
        fullScreenProgressBar.show()
        SystemClock.sleep(300)
        fullScreenProgressBar.hide()
        SystemClock.sleep(500)

        SystemClock.sleep(260) // Wait for animation
        onView(withId(1337)).check(matches(not(isDisplayed())))
    }

    class HolderFragment : Fragment() {
        lateinit var fullScreenProgressBar: FullScreenProgressBar

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return FullScreenProgressBar(requireContext())
                .also {
                    it.id = 1337
                    fullScreenProgressBar = it
                }
        }
    }
}