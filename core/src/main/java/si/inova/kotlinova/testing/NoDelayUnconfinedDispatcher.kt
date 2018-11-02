package si.inova.kotlinova.testing

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Runnable
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

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