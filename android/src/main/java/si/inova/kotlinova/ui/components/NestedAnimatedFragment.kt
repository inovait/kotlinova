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

import android.content.res.Resources
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.nextAnim
import timber.log.Timber

/**
 * Fragment base that fixes bug where child fragments disappear when animating
 * (https://issuetracker.google.com/issues/36970570)
 *
 * @author https://stackoverflow.com/a/23276145, adapted by Matej Drobnic
 */

open class NestedAnimatedFragment : StateSaverFragment() {
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val parent = parentFragment

        return if (!enter && parent?.isRemoving == true) {
            val dummyAnimation = AlphaAnimation(1f, 1f)
            dummyAnimation
                .duration =
                getNextAnimationDuration(parent, DEFAULT_CHILD_ANIMATION_DURATION.toLong())
            dummyAnimation
        } else {
            super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    companion object {
        // Arbitrary value; set it to some reasonable default
        private const val DEFAULT_CHILD_ANIMATION_DURATION = 500

        /**
         * @return Duration of the next animation (so child fragment is displayed for longer)
         */
        private fun getNextAnimationDuration(fragment: Fragment, defValue: Long): Long {
            try {
                // Attempt to get the resource ID of the next animation that
                // will be applied to the given fragment.
                val nextAnimResource = fragment.nextAnim()
                if (nextAnimResource <= 0) {
                    return defValue
                }

                val nextAnim = AnimationUtils.loadAnimation(fragment.activity, nextAnimResource)

                // ...and if it can be loaded, return that animation's duration
                return nextAnim?.duration ?: defValue
            } catch (ex: Resources.NotFoundException) {
                Timber.e(ex, "Fragment animation workaround failed")
                return defValue
            }
        }
    }
}
