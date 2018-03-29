@file:JvmName("ViewUtils")

package si.inova.kotlinova.utils

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v4.view.AsyncLayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.view.isVisible

/**
 * @author Matej Drobnic
 */

/**
 * Variant of [isVisible] that fades view in/out instead of just making it appear
 */
var View.isVisibleWithFade: Boolean
    get() = isVisible
    set(value) {
        if (value == isVisible) {
            return
        }

        val targetAlpha = if (value) 1f else 0f
        val startAlpha = 1f - targetAlpha

        alpha = startAlpha

        animate()
                .alpha(targetAlpha)
                .apply {
                    if (value) {
                        withStartAction { isVisible = true }
                    } else {
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
 * Hide soft keyboard
 */
fun View.hideKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
    )
}
