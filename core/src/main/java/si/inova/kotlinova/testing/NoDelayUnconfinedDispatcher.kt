package si.inova.kotlinova.testing

import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Delay
import kotlinx.coroutines.experimental.Runnable
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

/**
 * [CoroutineDispatcher] that removes all [delay] calls
 *
 * @author Matej Drobnic
 */
class NoDelayUnconfinedDispatcher : CoroutineDispatcher(), Delay {
    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: CancellableContinuation<Unit>
    ) {
        continuation.resume(Unit)
    }

    override fun scheduleResumeAfterDelay(
        time: Long,
        unit: TimeUnit,
        continuation: CancellableContinuation<Unit>
    ) {
        continuation.resume(Unit)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }
}