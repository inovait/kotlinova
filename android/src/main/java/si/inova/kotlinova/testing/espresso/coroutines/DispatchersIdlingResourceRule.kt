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

package si.inova.kotlinova.testing.espresso.coroutines

import androidx.test.espresso.IdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.collections.WeakList
import si.inova.kotlinova.coroutines.TestableDispatchers

/**
 * Espresso idling resource and test watcher that injects
 * wrapped dispatchers into [TestableDispatchers] and monitors for any leftover jobs.
 */
class DispatchersIdlingResourceRule : TestWatcher(), IdlingResource {
    private var idlingCallback: IdlingResource.ResourceCallback? = null
    private val allJobs = WeakList<JobCheckingDispatcherWrapper>()

    override fun starting(description: Description?) {
        super.starting(description)

        TestableDispatchers.dispatcherOverride = {
            val wrappedDispatcher = JobCheckingDispatcherWrapper(it() as CoroutineDispatcher)
            registerNewDispatcher(wrappedDispatcher)

            wrappedDispatcher
        }
    }

    override fun finished(description: Description?) {
        super.finished(description)

        TestableDispatchers.dispatcherOverride = { it() }
    }

    private fun registerNewDispatcher(dispatcherWrapper: JobCheckingDispatcherWrapper) {
        dispatcherWrapper.completionEvent = this::onJobStopped

        allJobs.add(dispatcherWrapper)
    }

    private fun onJobStopped() {
        if (isIdleNow) {
            idlingCallback?.onTransitionToIdle()
        }
    }

    override fun isIdleNow(): Boolean {
        var idle = true

        allJobs.forEach {
            if (it.isAnyJobRunning) {
                idle = false
            }
        }

        return idle
    }

    override fun getName(): String {
        return "DispatchersIdlingResourceRule"
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.idlingCallback = callback
    }
}