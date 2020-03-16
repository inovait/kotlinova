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

@file:JvmName("ViewUtils")

package si.inova.kotlinova.utils

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.SwitchCompat
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment

/**
 * @author Matej Drobnic
 */

/**
 * Variant of [isVisible] that fades view in/out instead of just making it appear
 */
var View.isVisibleWithFade: Boolean
    get() = isVisible
    set(value) {
        val targetAlpha = if (value) 1f else 0f

        if (!isVisible && value && alpha == 1f) {
            alpha = 0f
        }

        isVisible = true

        animate()
                .alpha(targetAlpha)
                .apply {
                    if (!value) {
                        withEndAction { isVisible = false }
                    }
                }
                .setDuration(250)
                .start()
    }

/**
 * Helper method to inflate given layout asynchronously
 */
fun Activity.inflateAsync(
        savedInstanceState: Bundle?,
        @LayoutRes layout: Int,
        callback: AsyncLayoutInflater.OnInflateFinishedListener
) {
    val root = findViewById<ViewGroup>(android.R.id.content)

    if (savedInstanceState == null) {
        AsyncLayoutInflater(this).inflate(layout, root, callback)
    } else {
        // If we are restoring state, we cannot inflate asynchronously as it messes with fragment lifecycle
        val view = layoutInflater.inflate(layout, root, false)
        callback.onInflateFinished(view, layout, root)
    }
}

/**
 * Helper method to inflate given layout asynchronously
 */
fun Fragment.inflateAsync(
        savedInstanceState: Bundle?,
        @LayoutRes layout: Int,
        callback: AsyncLayoutInflater.OnInflateFinishedListener
) {
    val root = view as ViewGroup

    if (savedInstanceState == null) {
        AsyncLayoutInflater(requireContext()).inflate(layout, root, callback)
    } else {
        // If we are restoring state, we cannot inflate asynchronously as it messes with fragment lifecycle
        val view = layoutInflater.inflate(layout, root, false)
        callback.onInflateFinished(view, layout, root)
    }
}

/**
 * Close soft keyboard
 */
fun View.closeKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
    )
}

/**
 * Open soft keyboard
 */
fun View.openKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.showSoftInput(
            this,
            InputMethodManager.SHOW_IMPLICIT
    )
}

/**
 * It makes view non-editable
 */
fun View.makeNonEditable() {
    isClickable = false
    isEnabled = false
}

/**
 * Set view visibility without triggering any layout animations
 * (for example animations that get triggered when animateLayoutChanges flag is enabled)
 */
fun View.setVisibilityWithoutLayoutAnimations(visibility: Int) {
    val transition = (parent as? ViewGroup)?.layoutTransition

    val appearingEnabled =
            transition?.isTransitionTypeEnabled(LayoutTransition.APPEARING) ?: false
    val disappearingEnabled =
            transition?.isTransitionTypeEnabled(LayoutTransition.DISAPPEARING) ?: false

    transition?.disableTransitionType(LayoutTransition.APPEARING)
    transition?.disableTransitionType(LayoutTransition.DISAPPEARING)

    this.visibility = visibility

    if (appearingEnabled) {
        transition!!.enableTransitionType(LayoutTransition.APPEARING)
    }
    if (disappearingEnabled) {
        transition!!.enableTransitionType(LayoutTransition.DISAPPEARING)
    }
}

fun SwitchCompat.setIsCheckedWithoutTriggeringListener(
        state: Boolean,
        listener: CompoundButton.OnCheckedChangeListener
) {
    setOnCheckedChangeListener(null)
    isChecked = state
    setOnCheckedChangeListener(listener)
}