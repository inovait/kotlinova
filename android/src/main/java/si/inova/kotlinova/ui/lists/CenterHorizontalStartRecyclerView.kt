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

package si.inova.kotlinova.ui.lists

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

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