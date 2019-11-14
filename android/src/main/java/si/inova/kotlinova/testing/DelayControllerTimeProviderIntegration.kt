/*
 * Copyright © 2016, Connected Travel, LLC – All Rights Reserved.
 *
 * All information contained herein is property of Connected Travel, LLC including, but
 * not limited to, technical and intellectual concepts which may be embodied within.
 *
 * Dissemination or reproduction of this material is strictly forbidden unless prior written
 * permission, via license, is obtained from Connected Travel, LLC. If permission is obtained,
 * this notice, and any other such legal notices, must remain unaltered.
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
 * Test rule that links [AndroidTimeProvider] and [JavaTimeProvider] with any [DelayController].
 *
 * To use this class, you can:
 *
 * 1. Have global [DelayController], set in constructor
 * 2. Manually call [register] method
 * 3. call [runBlockingTest] directly on this class instead of using native runBlockingTest.
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
        JavaTimeProvider.clockProvider =
            {
                java.time.Clock.fixed(
                    java.time.Instant.ofEpochMilli(controller.currentTime),
                    java.time.ZoneId.of("UTC")
                )
            }

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
        JavaTimeProvider.clockProvider = { java.time.Clock.systemUTC() }
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