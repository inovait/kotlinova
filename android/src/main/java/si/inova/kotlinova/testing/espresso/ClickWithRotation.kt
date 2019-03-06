/*
 * Copyright © 2016, Connected Travel, LLC – All Rights Reserved.
 *
 * All information contained herein is property of Connected Travel, LLC including, but
 * not limited to, technical and intellectual concepts which may be embodied within.
 *
 * Dissemination or reproduction of this material is strictly forbidden unless prior written
 * permission, via license, is obtained from Connected Travel, LLC. If permission is obtained,
 * this notice, and any other such legal notices, must remain unaltered.
 */

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