package si.inova.kotlinova.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.view.View
import java.util.WeakHashMap

/**
 * Class that handles setting and clearing view placeholders
 *
 * @param standardPlaceholderDrawableFactory factory method that creates drawables that will be used
 * for standard placeholders
 *
 * @author Matej Drobnic
 */
class PlaceholderManager(private val standardPlaceholderDrawableFactory: () -> Drawable) {
    private val oldDrawables: MutableMap<View, Drawable?> = WeakHashMap()

    /**
     * Apply standard placeholder on the specified view
     *
     * @param animate when *true* (default is *false*),
     * placeholder apply will be animated instead of being instantaneous
     */
    fun applyPlaceholder(vararg views: View, animate: Boolean = false) {
        for (view in views) {
            val targetDrawable = standardPlaceholderDrawableFactory()
            applyCustomPlaceholder(view, targetDrawable, animate)
        }
    }

    /**
     * Apply custom placeholder on the specified view
     *
     * @param animate when *true* (default is *false*),
     * placeholder apply will be animated instead of being instantaneous
     */
    fun applyCustomPlaceholder(view: View, placeholder: Drawable, animate: Boolean = false) {
        val currentDrawable = view.background

        if (animate && currentDrawable != null) {
            val transition = TransitionDrawable(
                arrayOf(
                    currentDrawable,
                    placeholder
                )
            ).apply { isCrossFadeEnabled = true }

            view.background = transition

            transition.startTransition(PLACEHOLDER_ANIMATION_DURATION)
        } else {
            view.background = placeholder
        }

        oldDrawables[view] = currentDrawable
    }

    /**
     * Clear placeholders from specific views
     *
     * @param animate when *true* (default), placeholder removal will be animated instead of
     * being instantaneous
     * @param force when *false* (default), only known views that have been manually applied using
     * [applyCustomPlaceholder] will be cleared. When set to *true*, all views's backgrounds will be
     * removed
     */
    fun clearPlaceholder(vararg views: View, animate: Boolean = true, force: Boolean = false) {
        for (view in views) {
            if (!force && !oldDrawables.containsKey(view)) {
                continue
            }

            val targetDrawable = oldDrawables[view]
            val currentDrawable = view.background

            if (animate && currentDrawable != null) {
                val transition = TransitionDrawable(
                    arrayOf(
                        currentDrawable,
                        targetDrawable ?: ColorDrawable(Color.TRANSPARENT)
                    )
                ).apply { isCrossFadeEnabled = true }

                view.background = transition

                transition.startTransition(PLACEHOLDER_ANIMATION_DURATION)
            } else {
                view.background = targetDrawable
            }

            oldDrawables.remove(view)
        }
    }
}

private const val PLACEHOLDER_ANIMATION_DURATION = 250