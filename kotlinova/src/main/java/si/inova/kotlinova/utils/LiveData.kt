@file:JvmName("LiveDataUtils")

package si.inova.kotlinova.utils

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import org.reactivestreams.Publisher
import si.inova.kotlinova.archcomponents.ResourcePublisherLiveData
import si.inova.kotlinova.data.resources.Resource

/**
 * @author Matej Drobnic
 */

/**
 * Convenience extension that allows putting method references into *LiveData.observe()*
 */
fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, method: Function1<T?, Unit>) {
    observe(lifecycleOwner, Observer { method(it) })
}

/**
 * Remove all observers from passed lifeCycleOwner that observe this [LiveData] and
 * add new observer that triggers supplied function
 *
 * This is useful with Fragments where onCreate* methods can be called multiple times in the same instance
 */
fun <T> LiveData<T>.exclusiveObserve(lifecycleOwner: LifecycleOwner, method: Function1<T?, Unit>) {
    removeObservers(lifecycleOwner)

    observe(lifecycleOwner, Observer {
        method(it)
    })
}

/**
 * Observe LiveData<Unit> as event with no-args method
 */
fun LiveData<Unit>.observeEvent(lifecycleOwner: LifecycleOwner, method: Function0<Unit>) {
    observe(lifecycleOwner, Observer { method() })
}

/**
 * Convenience extension that automatically filters *null* values received from [LiveData]
 */
fun <T> LiveData<T>.observeNotNull(lifecycleOwner: LifecycleOwner, method: Function1<T, Unit>) {
    observe(lifecycleOwner, Observer {
        if (it != null) {
            method(it)
        }
    })
}

/**
 * Remove all observers from passed lifeCycleOwner that observe this [LiveData] and
 * add new observer that automatically filters *null* values received from [LiveData]
 *
 * This is useful with Fragments where onCreate* methods can be called multiple times in the same instance
 */
fun <T> LiveData<T>.exclusiveObserveNotNull(
    lifecycleOwner: LifecycleOwner,
    method: Function1<T, Unit>
) {
    removeObservers(lifecycleOwner)

    observe(lifecycleOwner, Observer {
        if (it != null) {
            method(it)
        }
    })
}

/**
 * Method that redispatches current LiveData value to the observer (if applicable).
 */
inline fun <T> LiveData<T>.redispatch(observer: Function1<T, Unit>) {
    this.value?.let {
        observer(it)
    }
}

/**
 * Add source to the [MediatorLiveData] that just forwards its value to MediatorLiveData's value.
 */
fun <T> MediatorLiveData<T>.addSource(source: LiveData<T>) {
    addSource(source) {
        this.value = it
    }
}

/**
 * Add Rx source to the [MediatorLiveData].
 *
 * This should only be used when [T] is not [Resource] and thus [addResourceSource] cannot be used.
 */
fun <T, S> MediatorLiveData<T>.addSource(source: Publisher<S>, onChanged: (S?) -> Unit) {
    addSource(source.toLiveData(), onChanged)
}

/**
 * Add Rx source to the [MediatorLiveData] that just forwards its value to MediatorLiveData's value.
 *
 * This should only be used when [T] is not [Resource] and thus [addResourceSource] cannot be used.
 */
fun <T> MediatorLiveData<T>.addSource(source: Publisher<T>) {
    addSource(source.toLiveData())
}

/**
 * Add [Resource] Rx source to the [Resource][Resource] [MediatorLiveData].
 */
fun <T, S> MediatorLiveData<Resource<T>>.addResourceSource(
    source: Publisher<Resource<S>>,
    onChanged: (Resource<S>?) -> Unit
) {
    addSource(source.toResourceLiveData(), onChanged)
}

/**
 * Add [Resource] Rx source to the [Resource][Resource] [MediatorLiveData]
 * that just forwards its value to MediatorLiveData's value.
 */
fun <T> MediatorLiveData<Resource<T>>.addResourceSource(source: Publisher<Resource<T>>) {
    addSource(source.toResourceLiveData())
}

fun <S, R> LiveData<S>.map(transformation: (S?) -> R?): LiveData<R> {
    return Transformations.map(this, transformation)
}

/**
 * Similar to [map] except that no value will be emitted if transformations returns *null*.
 */
fun <S, R> LiveData<S>.mapIfNotNull(transformation: (S?) -> R?): LiveData<R> {
    val resultLiveData = MediatorLiveData<R>()
    resultLiveData.addSource(this) {
        val result = transformation(it)
        if (result != null) {
            resultLiveData.value = result
        }
    }
    return resultLiveData
}

fun <T> Publisher<T>.toLiveData(): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this)
}

fun <T> Publisher<Resource<T>>.toResourceLiveData(): LiveData<Resource<T>> {
    return ResourcePublisherLiveData(this)
}