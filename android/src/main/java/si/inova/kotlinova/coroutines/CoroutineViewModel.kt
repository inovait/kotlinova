package si.inova.kotlinova.coroutines

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import si.inova.kotlinova.data.SingleLiveEvent
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.data.resources.value
import si.inova.kotlinova.exceptions.OwnershipTransferredException
import si.inova.kotlinova.utils.runOnUiThread
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * ViewModel that automatically handles cancellation of all its coroutines when application
 * stops.
 *
 * Implementors must use [launchManaged] instead of [launch] to launch their coroutines
 *
 * @author Matej Drobnic
 */
abstract class CoroutineViewModel : ViewModel(), CoroutineScope {
    protected val parentJob: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = parentJob + TestableDispatchers.Default

    protected val activeJobs = ConcurrentHashMap<MutableLiveData<Resource<*>>, Job>()

    protected val _errors = SingleLiveEvent<Throwable>()
    /**
     * Common error observer. Used if *routeErrorsToCommonObserver* set to true when launching
     * resource tasks.
     */
    val errors: LiveData<Throwable> = _errors

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
     *
     * @param routeErrorsToCommonObservable If *true*, all errors from this resource will be
     * redirected to the [errors] observable instead of returning [Resource.Error]. Default value
     * of this parameter can be controlled using [routeErrorsToCommonObservableByDefault].
     */
    fun <T> launchResourceControlTask(
        resource: ResourceLiveData<T>,
        currentValue: T? = resource.value?.value,
        context: CoroutineContext = EmptyCoroutineContext,
        routeErrorsToCommonObservable: Boolean = routeErrorsToCommonObservableByDefault,
        block: suspend ResourceLiveData<T>.() -> Unit
    ) = runOnUiThread(parentJob) {
        // To prevent threading issues, only one job can handle one resource at a time
        // Cancel active job first.
        val currentJobForThatResource = activeJobs.remove(resource as MutableLiveData<*>)

        currentJobForThatResource?.cancel()

        if (resource.hasAnySources()) {
            resource.removeAllSources()
        }

        val newJob = launch(context, CoroutineStart.LAZY) {
            val thisJob = coroutineContext[Job]!!

            currentJobForThatResource?.join()

            if (!isActive) {
                return@launch
            }

            resource.interceptor = if (routeErrorsToCommonObservable) {
                {
                    if (it is Resource.Error<*>) {
                        routeException(resource, it.exception, routeErrorsToCommonObservable)
                        true
                    } else {
                        false
                    }
                }
            } else {
                null
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
                    withContext(Dispatchers.Main) {
                        resource.removeAllSources()
                    }
                }

                resource.sendValue(Resource.Error(e))
            } finally {
                @Suppress("UNCHECKED_CAST")
                activeJobs.remove(resource as MutableLiveData<Resource<*>>, thisJob)
            }
        }

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

    private fun <T> routeException(
        resource: ResourceLiveData<T>,
        exception: Throwable,
        routeErrorsToCommonObserver: Boolean
    ) {
        if (routeErrorsToCommonObserver) {
            resource.sendValueSync(Resource.Cancelled())

            if (!_errors.hasObservers()) {
                throw UndeliverableException(
                    "Exception could not be delivered because nobody is observing errors observer",
                    exception
                )
            }

            _errors.postValue(exception)
        } else {
            resource.sendValueSync(Resource.Error(exception))
        }
    }

    protected open val routeErrorsToCommonObservableByDefault: Boolean
        get() = false

    class UndeliverableException(message: String, cause: Throwable) : Exception(message, cause)
}