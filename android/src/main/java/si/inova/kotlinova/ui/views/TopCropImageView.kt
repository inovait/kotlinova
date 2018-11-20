package si.inova.kotlinova.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * ImageView that applies top crop scaling. This is similar to *centerCrop* scaling type of
 * regular ImageView, except that image is anchored to the top of the view instead of to the
 * center
 *
 * @author https://stackoverflow.com/a/6333518, adapted by Matej Drobnic
 */
class TopCropImageView : AppCompatImageView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val matrix = imageMatrix
        val scaleFactor = measuredWidth / drawable.intrinsicWidth.toFloat()
        matrix.setScale(scaleFactor, scaleFactor, 0f, 0f)
        imageMatrix = matrix
        return super.setFrame(l, t, r, b)
    }
}