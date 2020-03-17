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

package si.inova.kotlinova.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Utility class that manages lifecycle of multiple coroutines that manage the same resource.
 */
open class CoroutineResourceManager(protected val scope: CoroutineScope) {
    private val activeJobs = ConcurrentHashMap<Any, Job>()

    /**
     * Launch new coroutine, bound to the passed resource object.
     *
     * Any previous launched coroutines that were bound to an object equal to passed object will
     * be cancelled.
     */
    @Synchronized
    fun launchBoundControlTask(
        resource: Any,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend () -> Unit
    ) {
        // To prevent threading issues, only one job can handle one resource at a time
        // Cancel active job first.
        val currentJobForThatResource = activeJobs.remove(resource)

        currentJobForThatResource?.cancel()

        val newJob = scope.launch(context, CoroutineStart.LAZY) {
            val thisJob = coroutineContext[Job]!!

            currentJobForThatResource?.join()

            if (!isActive) {
                return@launch
            }

            try {
                block()
            } finally {
                activeJobs.remove(resource, thisJob)
            }
        }

        activeJobs[resource] = newJob

        newJob.start()
    }

    /**
     * @return whether resource is already being managed by a Job
     */
    fun isResourceTaken(resource: Any): Boolean {
        return activeJobs.containsKey(resource)
    }

    /**
     * Cancel job that currently manages passed resource.
     */
    fun cancelResource(resource: Any) {
        @Suppress("USELESS_CAST")
        val activeJob = activeJobs[resource]
        activeJob?.cancel()
    }

    /**
     * Get the job that currently manages passed resource.
     * This is only meant for read-only inspection. Do
     * NOT cancel the job. Use [cancelResource] method instead.
     */
    fun getCurrentJob(resource: Any): Job? {
        return activeJobs[resource]
    }
}