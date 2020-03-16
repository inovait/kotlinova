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

@file:JvmName("EspressoUtils")

package si.inova.kotlinova.testing

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not

fun hasImage(): Matcher<View> {
    return that("has image") { image: ImageView -> image.drawable != null }
}

/**
 * Assert that view either does not exist in the hierarchy or that view is not displayed
 */
fun ViewInteraction.assertNonexistentOrInvisible() {
    check(ViewNonexistentOrInvisible())
}

/**
 * Stop the test until RecyclerView's data gets loaded.
 *
 * Passed [recyclerProvider] will be activated in UI thread, allowing you to retrieve the View.
 *
 * Workaround for https://issuetracker.google.com/issues/123653014
 */
inline fun waitUntilLoaded(crossinline recyclerProvider: () -> RecyclerView) {
    Espresso.onIdle()

    lateinit var recycler: RecyclerView

    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        recycler = recyclerProvider()
    }

    while (recycler.hasPendingAdapterUpdates()) {
        Thread.sleep(10)
    }
}

class ViewNonexistentOrInvisible() : ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (view == null) {
            // View does not exist, test has passed
            return
        }

        val notDisplayedAssertion = matches(not(ViewMatchers.isDisplayed()))
        notDisplayedAssertion.check(view, noViewFoundException)
    }
}

/**
 * Loop until this specific idling resource becomes idle
 */
fun IdlingResource.waitUntilIdle(timeoutMs: Long = 5000, periodMs: Long = 10) {
    val end = System.currentTimeMillis() + timeoutMs

    while (!isIdleNow) {
        if (System.currentTimeMillis() >= end) {
            throw AssertionError("IdlingResource $this is not idle after timeout")
        }
        Thread.sleep(periodMs)
    }
}