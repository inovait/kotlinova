package si.inova.kotlinova.testing

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 * [CoroutineDispatcher] that removes all [delay] calls
 *
 * @author Matej Drobnic
 */
// Delay is internal, but there is no alternative and this is only used for tests
@UseExperimental(InternalCoroutinesApi::class)
class NoDelayUnconfinedDispatcher : CoroutineDispatcher(), Delay {
    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: CancellableContinuation<Unit>
    ) {
        continuation.resume(Unit)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }
}