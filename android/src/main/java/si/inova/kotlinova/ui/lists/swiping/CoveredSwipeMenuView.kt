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

package si.inova.kotlinova.ui.lists.swiping

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import si.inova.kotlinova.android.R
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Menu that can be either swiped away horizontally in either direction or it can be
 * moved slightly, revealing menu items on the left or right side.
 *
 * @author Matej Drobnic
 */
class CoveredSwipeMenuView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), HorizontalSwipeHelper.Listener {
    private var helper: HorizontalSwipeHelper? = null

    private var leftItem: View? = null
    private var rightItem: View? = null
    private var behindBackground: View? = null
    private var foregroundSwipingItem: View? = null

    private val leftItemId: Int
    private val rightItemId: Int
    private val behindBackgroundId: Int
    private val foregroundSwipeId: Int

    var onSwipeLeftListener: (() -> Unit)? = null
    var onSwipeRightListener: (() -> Unit)? = null
    var onMovingSwipeListener: (() -> Unit)? = null

    private val leftItemClipRect = Rect()
    private val rightItemClipRect = Rect()
    private val backgroundClipRect = Rect()

    var allowDragging = true

    init {
        val args = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CoveredSwipeMenuView,
                defStyleAttr, defStyleAttr
        )

        leftItemId = args.getResourceId(R.styleable.CoveredSwipeMenuView_leftItem, 0)
        rightItemId = args.getResourceId(R.styleable.CoveredSwipeMenuView_rightItem, 0)
        behindBackgroundId =
                args.getResourceId(R.styleable.CoveredSwipeMenuView_behindBackground, 0)
        foregroundSwipeId = args.getResourceId(R.styleable.CoveredSwipeMenuView_swipingItem, 0)
        args.recycle()
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)

        if (child.id == 0) {
            return
        }

        when (child.id) {
            leftItemId -> {
                leftItem = child
                child.isInvisible = true
            }
            rightItemId -> {
                rightItem = child
                child.isInvisible = true
            }
            behindBackgroundId -> {
                behindBackground = child
                child.isInvisible = true
            }
            foregroundSwipeId -> {
                foregroundSwipingItem = child
                helper = HorizontalSwipeHelper(child).also {
                    it.swipeListener = this
                }
                requestLayout()
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        helper?.let { helper ->
            helper.leftAnchor = leftItem?.width?.toFloat() ?: 0f
            helper.rightAnchor = rightItem?.width?.toFloat() ?: 0f
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!allowDragging) {
            return false
        }

        return helper?.onTouchEvent(event) ?: false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!allowDragging) {
            return false
        }

        val localHelper = helper
        return if (localHelper != null) {
            if (localHelper.onInterceptTouchEvent(ev)) {
                onMovingSwipeListener?.invoke()
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    override fun onSwipedLeft() {
        onSwipeLeftListener?.invoke()
    }

    override fun onSwipedRight() {
        onSwipeRightListener?.invoke()
    }

    override fun onMoved(translationX: Float) {
        clipView(translationX, behindBackground, backgroundClipRect, abs(translationX) >= 1)
        clipView(translationX, leftItem, leftItemClipRect, translationX > 0)
        clipView(translationX, rightItem, rightItemClipRect, translationX < 0)
    }

    private fun clipView(translationX: Float, view: View?, clipRect: Rect, show: Boolean) {
        if (view == null) {
            return
        }

        if (show) {
            view.isVisible = true

            if (translationX > 0) {
                clipRect.set(0, 0, translationX.roundToInt(), view.height)
            } else {
                clipRect.set(view.width + translationX.roundToInt(), 0, view.width, view.height)
            }
            view.clipBounds = clipRect
        } else {
            view.isInvisible = true
        }
    }

    fun resetDrag() {
        foregroundSwipingItem?.translationX = 0f
        onMoved(0f)
    }

    fun resetSmooth() {
        helper?.resetSmooth()
    }
}