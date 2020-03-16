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

import android.os.SystemClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.UncompletedCoroutinesError
import org.junit.rules.ExternalResource
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import si.inova.kotlinova.time.AndroidTimeProvider
import si.inova.kotlinova.time.JavaTimeProvider
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Test rule that links [AndroidTimeProvider] and [JavaTimeProvider] (partially)
 * with any [DelayController].
 *
 * To use this class, you can:
 *
 * 1. Have global [DelayController], set in constructor
 * 2. Manually call [register] method
 * 3. call [runBlockingTest] directly on this class instead of using native runBlockingTest.
 *
 * Note that this class only sets the *currentTimeMillis* of the [JavaTimeProvider],
 * Clock is not supported due to Android device compatibility.
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
class DelayControllerTimeProviderIntegration(
    private val autoRegisteredController: DelayController? = null
) : ExternalResource() {
    var initialization: (DelayController.() -> Unit)? = null

    fun register(controller: DelayController) {
        AndroidTimeProvider.elapsedRealtimeProvider = { controller.currentTime }
        AndroidTimeProvider.uptimeMillisProvider = { controller.currentTime }
        AndroidTimeProvider.clockProvider =
            { Clock.fixed(Instant.ofEpochMilli(controller.currentTime), ZoneId.of("UTC")) }
        JavaTimeProvider.currentTimeMillisProvider = { controller.currentTime }

        initialization?.invoke(controller)
    }

    override fun before() {
        if (autoRegisteredController != null) {
            register(autoRegisteredController)
        }
    }

    override fun after() {
        AndroidTimeProvider.elapsedRealtimeProvider = { SystemClock.elapsedRealtime() }
        AndroidTimeProvider.uptimeMillisProvider = { SystemClock.uptimeMillis() }
        AndroidTimeProvider.clockProvider = { Clock.systemUTC() }
        JavaTimeProvider.currentTimeMillisProvider = { System.currentTimeMillis() }
    }

    /**
     * Convenience function that runs test and automatically registers test context with this
     * rule
     *
     * @see kotlinx.coroutines.test.runBlockingTest
     */
    inline fun runBlockingTest(
        context: CoroutineContext = EmptyCoroutineContext,
        crossinline testBody: suspend TestCoroutineScope.() -> Unit
    ) {
        kotlinx.coroutines.test.runBlockingTest(context) {
            register(this)
            testBody()
        }
    }

    /**
     * Variant or [runBlockingTest] that does NOT automatically skip the delays
     *
     * @see kotlinx.coroutines.test.runBlockingTest
     */
    inline fun runBlockingTestWithoutAutoDelaySkipping(
        crossinline testBody: suspend TestCoroutineScope.() -> Unit
    ) {
        val context = SupervisorJob()
        val scope = TestCoroutineScope(context)
        register(scope)
        val deferred = scope.async {
            scope.testBody()
        }
        deferred.getCompletionExceptionOrNull()?.let {
            throw it
        }
        scope.cleanupTestCoroutines()
        val endingJobs = context.activeJobs()
        if (endingJobs.isNotEmpty()) {
            throw UncompletedCoroutinesError("Test finished with active jobs: $endingJobs")
        }
    }

    fun CoroutineContext.activeJobs(): Set<Job> {
        return checkNotNull(this[Job]).children.filter { it.isActive }.toSet()
    }
}