package si.inova.kotlinova.ui.lists

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.ViewTreeObserver

/**
 * Horizontal [RecyclerView] that will start/end with first/last item centered.
 * This view should NOT have any start or end padding
 *
 * Based on *WearableRecyclerView* from Wear support library.
 *
 * @author Matej Drobnic
 */
class CenterHorizontalStartRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private var setupCenterOnPreDraw = true

    init {
        clipToPadding = false
    }

    private fun setupCenteredPadding() {
        if (childCount < 1) {
            throw IllegalStateException(
                "Padding centering should never be called before children are added"
            )
        }

        // All the children in the view are the same size, as we set setHasFixedSize
        // to true, so the height of the first child is the same as all of them.
        val child = getChildAt(0)
        if (!ViewCompat.isLaidOut(child)) {
            throw IllegalStateException(
                "Padding centering should never be called before children are laid out"
            )
        }

        val childWidth = child.width
        // This is enough padding to center the child view in the parent.
        val desiredPadding = (width * 0.5f - childWidth * 0.5f).toInt()

        if (paddingLeft != desiredPadding) {
            // The view is symmetric along the vertical axis, so the top and bottom
            // can be the same.
            setPadding(desiredPadding, paddingTop, desiredPadding, paddingBottom)

            scrollBy(-desiredPadding, 0)
        }
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (childCount > 0) {
            setupCenterOnPreDraw = false
            setupCenteredPadding()
        }
        true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnPreDrawListener(preDrawListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnPreDrawListener(preDrawListener)
    }
}