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

package si.inova.kotlinova.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import si.inova.kotlinova.android.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@SuppressLint("AppCompatCustomView")
/**
 * Variant of [ImageView] that preserves specific aspect ratio when being automatically resized.
 *
 * View will be first measured normally in [fixedDimension] dimension, then other dimension will be
 * set as [fixedMultiplier] multiplier of fixed dimension.
 *
 * @author Matej Drobnic
 */
class AspectRatioImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    /**
     * Non-fixed dimension will be determined by fixed dimension multiplied by this.
     */
    var fixedMultiplier: Float = 1f
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * Dimension that will determined other dimension.
     *
     * Either [DIMENSION_WIDTH] or [DIMENSION_HEIGHT]
     */
    var fixedDimension: Int = DIMENSION_WIDTH
        set(value) {
            field = value
            requestLayout()
        }

    init {
        val args = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.AspectRatioImageView,
                0, 0
        )

        try {
            fixedMultiplier = args.getFloat(R.styleable.AspectRatioImageView_fixedMultiplier, 1f)
            fixedDimension = args
                    .getInt(R.styleable.AspectRatioImageView_fixedDimension, DIMENSION_WIDTH)
        } finally {
            args.recycle()
        }
    }

    @SuppressLint("SwitchIntDef")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int
        val height: Int
        if (fixedDimension == DIMENSION_WIDTH) {
            width = when (MeasureSpec.getMode(widthMeasureSpec)) {
                MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
                MeasureSpec.AT_MOST -> min(maxWidth, MeasureSpec.getSize(widthMeasureSpec))
                else -> {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                    measuredWidth
                }
            }

            height = (width * fixedMultiplier).roundToInt()
        } else {
            height = when (MeasureSpec.getMode(heightMeasureSpec)) {
                MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
                MeasureSpec.AT_MOST -> min(maxHeight, MeasureSpec.getSize(heightMeasureSpec))
                else -> {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                    measuredHeight
                }
            }

            width = (height * fixedMultiplier).roundToInt()
        }

        setMeasuredDimension(
                max(min(width, maxWidth), minimumWidth),
                max(min(height, maxHeight), minimumHeight)
        )
    }

    companion object {
        const val DIMENSION_WIDTH = 0
        const val DIMENSION_HEIGHT = 1
    }
}
