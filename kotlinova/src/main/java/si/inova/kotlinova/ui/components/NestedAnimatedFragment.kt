package si.inova.kotlinova.ui.components

import android.content.res.Resources
import android.support.v4.app.Fragment
import android.support.v4.app.nextAnim
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
