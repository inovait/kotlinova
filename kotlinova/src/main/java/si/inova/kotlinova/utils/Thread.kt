/**
 * @author Matej Drobnic
 */
@file:JvmName("ThreadUtils")

package si.inova.kotlinova.utils

import android.os.Looper
import kotlinx.coroutines.experimental.launch
import si.inova.kotlinova.coroutines.UI

/**
 * Run specified block on the UI thread.
 *
 * If caller is already on the UI thread, block is ran immediately synchronously.
 * Otherwise block is scheduled to run on UI thread scheduler.
 */
inline fun runOnUiThread(crossinline block: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        block()
    } else {
        launch(UI) {
            block()
        }
    }
}
