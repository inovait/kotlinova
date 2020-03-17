/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.testing

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.TestableDispatchers
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
@Suppress("EXPERIMENTAL_API_USAGE")
@Deprecated("Use CoroutinesTimeMachine or TestCoroutineDispatcher instead")
class TimedDispatcher : TestWatcher() {
    override fun finished(description: Description?) {
        super.finished(description)

        TestableDispatchers.dispatcherOverride = { it() }
        Dispatchers.resetMain()
    }

    override fun starting(description: Description?) {
        super.starting(description)

        TestableDispatchers.dispatcherOverride = { Dispatcher }
        Dispatchers.setMain(Dispatcher)
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