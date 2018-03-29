package si.inova.kotlinova.ui.views

import android.annotation.TargetApi
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.util.StateSet

/**
 * @author Matej Drobnic
 */

/**
 * @return ripple that ripples in provided color
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private fun createRipple(@ColorInt color: Int): RippleDrawable {
    val colorState = ColorStateList(
            arrayOf(intArrayOf()),
            intArrayOf(color)
    )

    return RippleDrawable(colorState, null, ColorDrawable(Color.BLACK))
}

/**
 * Method to create legacy (color switching) press effect for pre-Lollipop devices.
 *
 * @return Drawable that is normally transparent but changes to provided color when pressed.
 */
private fun createLegacyPressEffect(@ColorInt color: Int): StateListDrawable {
    val normalDrawable = ColorDrawable(Color.TRANSPARENT)
    val pressedDrawable = ColorDrawable(color)

    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        addState(StateSet.WILD_CARD, normalDrawable)
    }
}

/**
 * @return Press effect with specific color.
 * Depending on the platform it can be ripple or standard color changing drawable.
 */
fun createPressEffect(@ColorInt color: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        createRipple(color)
    } else {
        createLegacyPressEffect(color)
    }
}