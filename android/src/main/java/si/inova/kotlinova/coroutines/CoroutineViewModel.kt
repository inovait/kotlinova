package si.inova.kotlinova.coroutines

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.data.resources.value
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * ViewModel that automatically handles cancellation of all its coroutines when application
 * stops.
 *
 * Implementors must use [launchManaged] instead of [launch] to launch their coroutines
 *
 * Note that this class will not extend CoroutineScope in the future. For all launches, you must
 * call *scope* explicitly.
 *
 * @author Matej Drobnic
 */
abstract class CoroutineViewModel : ViewModel(),
    CoroutineScope by CoroutineScope(SupervisorJob() + TestableDispatchers.Default) {
    protected val scope = this

    protected val resources = LiveDataCoroutineResourceManager(scope)

    /**
     * Common error observer. Used if *routeErrorsToCommonObserver* set to true when launching
     * resource tasks.
     */
    val errors: LiveData<Throwable>
        get() = resources.errors

    init {
        @Suppress("DEPRECATION")
        resources.routeErrorsToCommonObservableByDefault = routeErrorsToCommonObservableByDefault
    }

    /**
     * @see LiveDataCoroutineResourceManager.launchResourceControlTask
     */
    @Deprecated(
        "Use resources.launchResourceControlTask instead"
    )
    fun <T> launchResourceControlTask(
        resource: ResourceLiveData<T>,
        currentValue: T? = resource.value?.value,
        context: CoroutineContext = EmptyCoroutineContext,
        routeErrorsToCommonObservable: Boolean = resources.routeErrorsToCommonObservableByDefault,
        block: suspend ResourceLiveData<T>.() -> Unit
    ) = resources.launchResourceControlTask(
        resource,
        currentValue,
        context,
        routeErrorsToCommonObservable,
        block
    )

    /**
     * @see CoroutineResourceManager.isResourceTaken
     */
    @Deprecated(
        "Use resources.isResourceTaken instead"
    )
    protected fun <T> isResourceTaken(resource: MutableLiveData<Resource<T>>): Boolean {
        return resources.isResourceTaken(resource)
    }

    /**
     * Cancel job that currently manages passed resource.
     */
    @Deprecated(
        "Use resources.cancelResource instead"
    )
    protected fun <T> cancelResource(resource: MutableLiveData<Resource<T>>) {
        resources.cancelResource(resource)
    }

    @CallSuper
    public override fun onCleared() {
        scope.cancel()

        super.onCleared()
    }

    @Deprecated("Set resources.routeCommonErrorsToCommonObservableByDefault instead")
    protected open val routeErrorsToCommonObservableByDefault: Boolean
        get() = false
}