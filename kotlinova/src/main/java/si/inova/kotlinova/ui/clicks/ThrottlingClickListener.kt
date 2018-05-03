package si.inova.kotlinova.ui.clicks

import android.os.SystemClock
import android.view.View

/**
 * Click listener that only gets triggered at most every 500 milliseconds. Use this to take care
 * of user spamming clicks.
 *
 * @author Jan Grah
 */
class ThrottlingClickListener(private val block: (View) -> Unit) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < MINIMUM_TIME_BETWEEN_CLICKS) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        block(view)
    }
}

private const val MINIMUM_TIME_BETWEEN_CLICKS = 500