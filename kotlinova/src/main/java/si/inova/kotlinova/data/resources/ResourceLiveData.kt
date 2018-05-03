package si.inova.kotlinova.data.resources

import android.arch.lifecycle.LiveData
import android.support.annotation.UiThread
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import si.inova.kotlinova.data.ExtendedMediatorLiveData
import si.inova.kotlinova.utils.isActive
import kotlin.coroutines.experimental.coroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * ViewModel that stores resource and receives data from coroutines on any threads.
 *
 * It includes synchronization mechanisms to make sure threaded data does not arrive out-of-order.
 * and property delegate to quickly create public-facing raw LiveData properties.
 *
 * @author Matej Drobnic
 */
class ResourceLiveData<T> : ExtendedMediatorLiveData<Resource<T>>(),
    ReadOnlyProperty<Any, LiveData<Resource<T>>> {
    private val resourceMutex: Mutex = Mutex()

    @Synchronized
    @Deprecated("Use sendValue() instead", ReplaceWith("sendValue(value)"))
    override fun postValue(value: Resource<T>) {
        throw IllegalStateException(
            "postValue is not supported for ResourceLiveData. " +
                "Use sendValue instead."
        )
    }

    suspend fun sendValue(value: Resource<T>) {
        resourceMutex.withLock {
            if (coroutineContext.isActive) {
                super.postValue(value)
            }
        }
    }

    /**
     * Send current value to this LiveData without coroutines.
     * */
    @Suppress("NOTHING_TO_INLINE")
    @UiThread
    inline fun sendValueSync(value: Resource<T>?) {
        super.postValue(value)
    }

    @Deprecated(
        "Use sendValueSync() instead",
        replaceWith = ReplaceWith("sendValueSync(value)")
    )
    override fun setValue(value: Resource<T>?) {
        @Suppress("DEPRECATION")
        super.setValue(value)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): LiveData<Resource<T>> {
        return this
    }
}