package si.inova.kotlinova.testing.espresso

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import si.inova.kotlinova.testing.that

/**
 * Alternative to the default click() action with espresso that also supports rotated views.
 *
 * See https://issuetracker.google.com/issues/64758984
 */
class ClickWithRotation() : ViewAction {
    override fun getDescription(): String {
        return "Click"
    }

    override fun getConstraints(): Matcher<View> {
        return that { it.isClickable }
    }

    override fun perform(uiController: UiController, view: View) {
        view.performClick()
    }
}