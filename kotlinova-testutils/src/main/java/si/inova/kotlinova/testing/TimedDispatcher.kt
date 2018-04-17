package si.inova.kotlinova.testing

import android.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Delay
import kotlinx.coroutines.experimental.DisposableHandle
import kotlinx.coroutines.experimental.Runnable
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.dispatcherOverride
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Rule that allows time-based dispatching without Robolectric dependency
 *
 * @author Matej Drobnic
 */
class TimedDispatcher : InstantTaskExecutorRule() {
    override fun finished(description: Description?) {
        super.finished(description)

        dispatcherOverride = { it() }
    }

    override fun starting(description: Description?) {
        super.starting(description)

        dispatcherOverride = { Dispatcher }
    }

    fun advanceTime(ms: Int) {
        Dispatcher.advanceTime(ms)
    }

    private object Dispatcher : CoroutineDispatcher(), Delay {
        private var currentTime = 0L

        private var schedules = ArrayList<Pair<Long, java.lang.Runnable>>()

        fun advanceTime(ms: Int) {
            currentTime += ms

            val runnablesToRun = ArrayList<Pair<Long, java.lang.Runnable>>()
            val iterator = schedules.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.first <= currentTime) {
                    runnablesToRun.add(next)
                    iterator.remove()
                }
            }

            runnablesToRun.sortedBy { it.first }.forEach { it.second.run() }
        }

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            block.run()
        }

        override fun scheduleResumeAfterDelay(
            time: Long,
            unit: TimeUnit,
            continuation: CancellableContinuation<Unit>
        ) {
            val target = Pair(
                currentTime + unit.toMillis(time),
                Runnable { with(continuation) { resumeUndispatched(Unit) } })
            schedules.add(target)
        }

        override fun invokeOnTimeout(
            time: Long,
            unit: TimeUnit,
            block: Runnable
        ): DisposableHandle {
            val target = Pair(currentTime + unit.toMillis(time), block)

            schedules.add(target)

            return object : DisposableHandle {
                override fun dispose() {
                    schedules.remove(target)
                }
            }
        }
    }
}