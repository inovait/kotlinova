package si.inova.kotlinova.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * Created on 10.4.2017.
 * A variant of [android.widget.FrameLayout] that completely disables touch events on views behind it
 *
 * @author Bojan Kseneman, adapted by Matej Drobnic
 */
open class TouchTakeOverFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
        FrameLayout(context, attrs, defStyleAttr) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }
}