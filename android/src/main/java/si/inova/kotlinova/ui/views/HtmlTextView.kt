package si.inova.kotlinova.ui.views

import android.content.Context
import android.os.Build
import android.support.v7.widget.AppCompatTextView
import android.text.Html
import android.util.AttributeSet
import android.widget.TextView

/**
 * [TextView] that parses HTML tags inside text and displays them.
 *
 * @author Matej Drobnic
 */
class HtmlTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    override fun setText(text: CharSequence?, type: BufferType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            super.setText(Html.fromHtml(text.toString(), 0), TextView.BufferType.SPANNABLE)
        } else {
            @Suppress("DEPRECATION")
            super.setText(Html.fromHtml(text.toString()), TextView.BufferType.SPANNABLE)
        }
    }
}