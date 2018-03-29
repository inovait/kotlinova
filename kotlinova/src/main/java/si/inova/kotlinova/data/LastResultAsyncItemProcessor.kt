package si.inova.kotlinova.data

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import si.inova.kotlinova.coroutines.CommonPool
import si.inova.kotlinova.coroutines.UI

/**
 * Class that helps with processing stream of items asynchronously.
 *
 * Callback is always called only for the last item
 * (e.g. even if you call process() 10000 times, it will only receive callback for last result)
 *
 * @author Matej Drobnic
 */
class LastResultAsyncItemProcessor<I, O> {
    @Volatile
    private var lastJob: Job? = null

    fun process(input: I, callback: (O) -> Unit, process: suspend CoroutineScope.(I) -> O) {
        lastJob?.cancel()

        lastJob = launch(UI) {
            val result = withContext(CommonPool) {
                process(input)
            }

            if (isActive) {
                callback(result)
            }
        }
    }
}

fun <T> LastResultAsyncItemProcessor<Unit, T>.process(
    callback: (T) -> Unit,
    process: suspend CoroutineScope.(Unit) -> T
) {
    process(Unit, callback, process)
}