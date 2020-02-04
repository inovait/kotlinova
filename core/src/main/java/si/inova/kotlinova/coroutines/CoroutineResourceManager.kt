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