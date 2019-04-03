@file:JvmName("EspressoUtils")

package si.inova.kotlinova.testing

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
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