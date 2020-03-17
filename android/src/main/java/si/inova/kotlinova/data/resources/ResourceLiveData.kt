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

package si.inova.kotlinova.data.resources

import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import si.inova.kotlinova.data.ExtendedMediatorLiveData
import si.inova.kotlinova.utils.isActive
import si.inova.kotlinova.utils.runOnUiThread
import kotlin.coroutines.coroutineContext
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
    /**
     * Interceptor that intercepts any incoming values. When returning [true], value is consumed and
     * is not forwarded to observers.
     */
    var interceptor: ((Resource<T>?) -> Boolean)? = null

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
        if (interceptor?.invoke(value) == true) {
            return
        }

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
        if (interceptor?.invoke(value) == true) {
            return
        }

        super.postValue(value)
    }

    override fun setValue(value: Resource<T>?) {
        if (interceptor?.invoke(value) == true) {
            return
        }

        super.setValue(value)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): LiveData<Resource<T>> {
        return this
    }

    override fun <S> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        runOnUiThread {
            super.addSource(source, onChanged)
        }
    }

    override fun <S> removeSource(source: LiveData<S>) {
        runOnUiThread {
            super.removeSource(source)
        }
    }
}

fun isAnyLoading(vararg resource: LiveData<*>): Boolean {
    return resource.any { it.value is Resource.Loading<*> }
}