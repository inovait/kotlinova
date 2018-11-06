package si.inova.kotlinova.coroutines

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.data.resources.value
import si.inova.kotlinova.exceptions.OwnershipTransferredException
import si.inova.kotlinova.utils.runOnUiThread
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * ViewModel that automatically handles cancellation of all its coroutines when application
 * stops.
 *
 * Implementors must use [launchManaged] instead of [launch] to launch their coroutines
 *
 * @author Matej Drobnic
 */
abstract class CoroutineViewModel : ViewModel() {
    protected val parentJob: Job = Job()

    protected val activeJobs = ConcurrentHashMap<MutableLiveData<Resource<*>>, Job>()

    /**
     * Launch automatically-cancelled job
     */
    fun launchManaged(
        context: CoroutineContext = TestableDispatchers.Default,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return GlobalScope.launch(context + parentJob, start, {
            block()
        })
    }

    /**
     * Method that launches coroutine task that handles data fetching for provided resources.
     *
     * This method:
     *
     * 1. Cancels any previous coroutines using this resource to prevent clashing
     * 2. Automatically puts resource into loading state with previous data as partial data
     * 3. Calls your provided task on the worker thread
     * 4. Automatically handles cancellation exceptions
     * 5. Automatically forwards exceptions to the resource as [Resource.Error]
     */
    fun <T> launchResourceControlTask(
        resource: ResourceLiveData<T>,
        currentValue: T? = resource.value?.value,
        context: CoroutineContext = TestableDispatchers.Default,
        block: suspend ResourceLiveData<T>.() -> Unit
    ) = runOnUiThread(parentJob) {
        // To prevent threading issues, only one job can handle one resource at a time
        // Cancel active job first.
        val currentJobForThatResource = activeJobs.remove(resource as MutableLiveData<*>)

        currentJobForThatResource?.cancel()

        if (resource.hasAnySources()) {
            resource.removeAllSources()
        }

        val newJob = GlobalScope.launch(context + parentJob, CoroutineStart.LAZY, {
            val thisJob = coroutineContext[Job]!!

            currentJobForThatResource?.join()

            if (!isActive) {
                return@launch
            }

            try {
                resource.sendValue(Resource.Loading(currentValue))

                block(resource)
            } catch (_: OwnershipTransferredException) {
                // Do nothing. New owner will set their own values.
            } catch (_: CancellationException) {
                @Suppress("USELESS_CAST")
                val meActive = activeJobs[resource as MutableLiveData<*>] == thisJob
                if (meActive) {
                    // Only post cancelled if I'm the currently active job
                    withContext(NonCancellable) {
                        resource.sendValue(Resource.Cancelled())
                    }
                }
            } catch (e: Exception) {
                if (resource.hasAnySources()) {
                    withContext(TestableDispatchers.Main) {
                        resource.removeAllSources()
                    }
                }

                resource.sendValue(Resource.Error(e))
            } finally {
                @Suppress("UNCHECKED_CAST")
                activeJobs.remove(resource as MutableLiveData<Resource<*>>, thisJob)
            }
        })

        @Suppress("UNCHECKED_CAST")
        activeJobs[resource as MutableLiveData<Resource<*>>] = newJob

        newJob.start()
    }

    /**
     * @return whether resource is already being managed by a Job
     */
    protected fun <T> isResourceTaken(resource: MutableLiveData<Resource<T>>): Boolean {
        return activeJobs.containsKey(resource as MutableLiveData<*>)
    }

    /**
     * Cancel job that currently manages passed resource.
     */
    protected fun <T> cancelResource(resource: MutableLiveData<Resource<T>>) {
        @Suppress("USELESS_CAST")
        val activeJob = activeJobs[resource as MutableLiveData<*>]
        activeJob?.cancel()
    }

    @CallSuper
    public override fun onCleared() {
        parentJob.cancel()

        super.onCleared()
    }
}