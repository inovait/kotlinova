@file:JvmName("LiveDataUtils")

package si.inova.kotlinova.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.reactivestreams.Publisher
import si.inova.kotlinova.archcomponents.ResourcePublisherLiveData
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.value
import kotlin.reflect.KProperty

/**
 * @author Matej Drobnic
 */

/**
 * Convenience extension that allows putting method references into *LiveData.observe()*
 */
inline fun <T> LiveData<T>.observe(
    lifecycleOwner: LifecycleOwner,
    crossinline method: Function1<T?, Unit>
) {
    observe(lifecycleOwner, Observer { method(it) })
}

/**
 * Remove all observers from passed lifeCycleOwner that observe this [LiveData] and
 * add new observer that triggers supplied function
 *
 * This is useful with Fragments where onCreate* methods
 * can be called multiple times in the same instance
 */
inline fun <T> LiveData<T>.exclusiveObserve(
    lifecycleOwner: LifecycleOwner,
    crossinline method: Function1<T?, Unit>
) {
    removeObservers(lifecycleOwner)

    observe(lifecycleOwner, Observer {
        method(it)
    })
}

/**
 * Observe LiveData<Unit> as event with no-args method
 */
inline fun LiveData<Unit>.observeEvent(
    lifecycleOwner: LifecycleOwner,
    crossinline method: Function0<Unit>
) {
    observe(lifecycleOwner, Observer { method() })
}

/**
 * Remove all observers from passed lifeCycleOwner that observe this [LiveData] and
 * add new observer that observes this LiveData<Unit> as event with no-args method
 *
 * This is useful with Fragments where onCreate* methods
 * can be called multiple times in the same instance
 */
inline fun LiveData<Unit>.exclusiveObserveEvent(
    lifecycleOwner: LifecycleOwner,
    crossinline method: Function0<Unit>
) {
    removeObservers(lifecycleOwner)

    observe(lifecycleOwner, Observer { method() })
}

/**
 * Convenience extension that observes the value of the Resource, regardless of its state
 */
inline fun <T> LiveData<T>.observeNotNull(
    lifecycleOwner: LifecycleOwner,
    crossinline method: Function1<T, Unit>
) {
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
 * This is useful with Fragments where onCreate* methods
 * can be called multiple times in the same instance
 */
inline fun <T> LiveData<T>.exclusiveObserveNotNull(
    lifecycleOwner: LifecycleOwner,
    crossinline method: Function1<T, Unit>
) {
    removeObservers(lifecycleOwner)

    observe(lifecycleOwner, Observer {
        if (it != null) {
            method(it)
        }
    })
}

/**
 * Remove all observers from passed lifeCycleOwner that observe this [LiveData] and
 * add new observer that observes value of this resource, regardless of its state
 *
 * This is useful with Fragments where onCreate* methods
 * can be called multiple times in the same instance
 */
inline fun <T> LiveData<Resource<T>>.exclusiveObserveResourceValue(
    lifecycleOwner: LifecycleOwner,
    crossinline method: Function1<T, Unit>
) {
    removeObservers(lifecycleOwner)

    observe(lifecycleOwner, Observer { resource ->
        val value = resource.value
        if (value != null) {
            method(value)
        }
    })
}

/**
 * Method that redispatches current LiveData value to the observer (if applicable).
 */
inline fun <T> LiveData<T>.redispatch(crossinline observer: Function1<T, Unit>) {
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
fun <T, S> MediatorLiveData<T>.addResourceSource(
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

fun <S, R> LiveData<S>.switchMap(transformation: (S?) -> LiveData<R>?): LiveData<R> {
    return Transformations.switchMap(this, transformation)
}

/**
 * Similar to [map] except that no value will be emitted if transformations returns *null*.
 */
inline fun <S, R> LiveData<S>.mapIfNotNull(crossinline transformation: (S?) -> R?): LiveData<R> {
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

fun <T> LiveData<T>.toMediator(): MediatorLiveData<T> {
    return MediatorLiveData<T>().also {
        it.addSource(this)
    }
}

fun <T> liveDataOf(value: T?): LiveData<T> {
    return MutableLiveData<T>().apply { this.value = value }
}

/**
 * Operator extension that allows implementing any LiveData as property delegate
 *
 * For example:
 *
 *     private val _data = MutableLiveData<String>()
 *     val data by _data
 */
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> LiveData<T>.getValue(thisRef: Any?, property: KProperty<*>): LiveData<T> =
        this