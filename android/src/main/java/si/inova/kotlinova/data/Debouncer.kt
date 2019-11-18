package si.inova.kotlinova.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Class that will only execute passed task if a particular time span has been passed without
 * another task being added.
 */
class Debouncer(
    private val debouncingTimeMs: Long = 500L,
    private val targetContext: CoroutineContext = Dispatchers.Main
) {
    private var previousJob: Job? = null

    fun executeDebouncing(task: () -> Unit) {
        previousJob?.cancel()
        previousJob = GlobalScope.launch(targetContext) {
            delay(debouncingTimeMs)
            task()
        }
    }
}