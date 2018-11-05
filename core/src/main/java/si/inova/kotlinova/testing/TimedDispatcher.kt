package si.inova.kotlinova.testing

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.dispatcherOverride
import java.util.PriorityQueue
import kotlin.coroutines.CoroutineContext
import kotlin.math.sign

/**
 * Rule that allows time-based dispatching without Robolectric dependency
 *
 * This class is only kept in for legacy purposes. For new projects you should use
 * [CoroutinesTimeMachine] instead.
 *
 * @author Matej Drobnic
 */
class TimedDispatcher : TestWatcher() {
    override fun finished(description: Description?) {
        super.finished(description)

        dispatcherOverride = { it() }
    }

    override fun starting(description: Description?) {
        super.starting(description)

        dispatcherOverride = { Dispatcher }
    }

    fun advanceTime(ms: Long) {
        Dispatcher.advanceTime(ms)
    }

    val coroutinesDispatcher: CoroutineContext
        get() = Dispatcher

    // Delay is internal, but there is no alternative and this is only used for tests
    @UseExperimental(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private object Dispatcher : CoroutineDispatcher(), Delay {
        private var currentTime = 0L
        private val schedules = PriorityQueue<ScheduledTask>()

        fun advanceTime(ms: Long) {
            val targetTime = currentTime + ms

            var next = schedules.peek()
            while (next != null && next.targetTime <= targetTime) {
                schedules.remove()
                currentTime = next.targetTime
                next.task.run()

                next = schedules.peek()
            }

            currentTime = targetTime
        }

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            block.run()
        }

        override fun scheduleResumeAfterDelay(
            timeMillis: Long,
            continuation: CancellableContinuation<Unit>
        ) {
            val target = ScheduledTask(
                currentTime + timeMillis,
                Runnable { with(continuation) { resumeUndispatched(Unit) } })
            schedules.add(target)
        }

        override fun invokeOnTimeout(timeMillis: Long, block: Runnable): DisposableHandle {
            val target = ScheduledTask(
                currentTime + timeMillis, block
            )

            schedules.add(target)

            return object : DisposableHandle {
                override fun dispose() {
                    schedules.remove(target)
                }
            }
        }

        private data class ScheduledTask(
            val targetTime: Long,
            val task: Runnable
        ) : Comparable<ScheduledTask> {
            override fun compareTo(other: ScheduledTask): Int {
                return (targetTime - other.targetTime).sign
            }
        }
    }
}