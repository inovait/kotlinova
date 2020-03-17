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

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import si.inova.kotlinova.data.SingleLiveEvent
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.data.resources.value
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

class LiveDataCoroutineResourceManager(scope: CoroutineScope) : CoroutineResourceManager(scope) {
    var routeErrorsToCommonObservableByDefault: Boolean = false

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
    ) = launchBoundControlTask(resource, context) {
        if (resource.hasAnySources()) {
            withContext(Dispatchers.Main) {
                resource.removeAllSources()
            }
        }

        val thisJob = coroutineContext[Job]!!

        setupInterceptor(resource, routeErrorsToCommonObservable)

        try {
            resource.sendValue(Resource.Loading(currentValue))

            block(resource)
        } catch (_: CancellationException) {
            val meActive = getCurrentJob(resource) == thisJob
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
        }
    }

    private fun <T> setupInterceptor(
        resource: ResourceLiveData<T>,
        routeErrorsToCommonObservable: Boolean
    ) {
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

    class UndeliverableException(message: String, cause: Throwable) : Exception(message, cause)
}