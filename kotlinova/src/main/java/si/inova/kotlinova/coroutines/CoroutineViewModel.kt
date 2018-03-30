package si.inova.kotlinova.coroutines

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import android.support.v4.util.SimpleArrayMap
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import si.inova.kotlinova.data.ExtendedMediatorLiveData
import si.inova.kotlinova.data.Resource
import si.inova.kotlinova.exceptions.OwnershipTransferredException
import kotlin.coroutines.experimental.CoroutineContext

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

    private val activeJobs = SimpleArrayMap<MutableLiveData<Resource<*>>, Job>()

    /**
     * Launch automatically-cancelled job
     */
    fun launchManaged(
        context: CoroutineContext = UI,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launch(context, start, parentJob) {
            block()
        }
    }

    /**
     * Convenience method that helps managing resources (*MutableLiveData&lt;Resource&gt;*). It:
     *
     * 1. Cancels any previous coroutines using this resource to prevent clashing
     * 2. Automatically puts resource into loading state
     * 3. Automatically handles cancellation exceptions
     * 4. Automatically forwards exceptions to the resource as [Resource.Error]
     */
    suspend fun <T, L : MutableLiveData<Resource<T>>> CoroutineScope.acquireResource(
        resource: L,
        currentValue: T? = null,
        unique: Boolean = true,
        block: suspend CoroutineScope.(L) -> Unit
    ) {
        // Make sure job cancellation runs on the same thread to prevent thread clashes
        withContext<Unit>(UI) {
            if (unique) {
                // To prevent threading issues, only one job can handle one resource at a time
                // Cancel active job first.
                val currentJobForThatResource = activeJobs.remove(resource)
                currentJobForThatResource?.let {
                    it.cancel(OwnershipTransferredException())
                    it.join()
                }

                // If this resource can be controlled from outside, we need to reset
                // it first.
                if (resource is ExtendedMediatorLiveData<*>) {
                    resource.removeAllSources()
                }
            }

            @Suppress("UNCHECKED_CAST")
            activeJobs.put(resource as MutableLiveData<Resource<*>>, coroutineContext[Job]!!)
        }

        try {
            resource.value = Resource.Loading(currentValue)
            block(resource)
        } catch (_: OwnershipTransferredException) {
            // Do nothing. New owner will set their own values.
        } catch (_: CancellationException) {
            resource.value = Resource.Cancelled()
        } catch (e: Exception) {
            resource.value = Resource.Error(e)
        } finally {
            activeJobs.remove(resource)
        }
    }

    /**
     * @return whether resource is already being managed by a Job
     */
    protected fun <T> isResourceTaken(resource: MutableLiveData<Resource<T>>): Boolean {
        return activeJobs.containsKey(resource)
    }

    /**
     * Cancel job that currently manages passed resource.
     *
     * @return *true* if any job was cancelled or *false* if nothing was managing that resource
     */
    protected fun <T> cancelResource(resource: MutableLiveData<Resource<T>>): Boolean {
        val activeJob = activeJobs.remove(resource)

        activeJob?.cancel()
        return activeJob != null
    }

    @CallSuper
    public override fun onCleared() {
        parentJob.cancel()

        super.onCleared()
    }
}