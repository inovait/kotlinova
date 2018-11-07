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

        adjustViewBounds = true
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
