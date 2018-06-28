package si.inova.kotlinova.data

import android.os.Handler
import android.os.Looper

/**
 * Class that will only execute passed task if a particular time span has been passed without
 * another task being added.
 *
 * @author Matej Drobnic
 */
class Debouncer(
    private val debouncingTimeMs: Long = 500L,
    targetLooper: Looper = Looper.getMainLooper()
) {
    val debounceHandler = Handler(targetLooper)

    fun executeDebouncing(task: () -> Unit) {
        debounceHandler.removeCallbacksAndMessages(null)
        debounceHandler.postDelayed(task, debouncingTimeMs)
    }
}