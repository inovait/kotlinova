package si.inova.kotlinova.ui.clicks

import android.os.SystemClock
import android.view.View

/**
 * @author Jan Grah
 */
class ThrottlingClickListener(private val block: (View) -> Unit) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        block(view)
    }
}