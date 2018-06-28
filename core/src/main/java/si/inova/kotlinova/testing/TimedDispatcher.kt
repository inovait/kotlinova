package si.inova.kotlinova.testing

import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Delay
import kotlinx.coroutines.experimental.DisposableHandle
import kotlinx.coroutines.experimental.Runnable
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.dispatcherOverride
import java.util.PriorityQueue
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.math.sign

/**
 * Rule that allows time-based dispatching without Robolectric dependency
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

    fun advanceTime(ms: Int) {
        Dispatcher.advanceTime(ms)
    }

    private object Dispatcher : CoroutineDispatcher(), Delay {
        private var currentTime = 0L
        private val schedules = PriorityQueue<ScheduledTask>()

        fun advanceTime(ms: Int) {
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
            time: Long,
            unit: TimeUnit,
            continuation: CancellableContinuation<Unit>
        ) {
            val target = ScheduledTask(
                currentTime + unit.toMillis(
                    time
                ),
                Runnable { with(continuation) { resumeUndispatched(Unit) } })
            schedules.add(target)
        }

        override fun invokeOnTimeout(
            time: Long,
            unit: TimeUnit,
            block: Runnable
        ): DisposableHandle {
            val target = ScheduledTask(
                currentTime + unit.toMillis(
                    time
                ), block
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