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

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.utils.addResourceSource

/**
 * Add Flow source to the [MediatorLiveData].
 *
 * This should only be used when [T] is not [Resource] and thus [addResourceSource] cannot be used.
 *
 * @param timeoutInMs The timeout duration before cancelling the block if
 * there are no active observers
 */
fun <T, S> MediatorLiveData<T>.addSource(
    source: Flow<S>,
    timeoutInMs: Long = DEFAULT_TIMEOUT,
    onChanged: (S?) -> Unit
) {
    addSource(liveData<S?>(timeoutInMs = timeoutInMs) {
        source.collect {
            emit(it)
        }
    }, onChanged)
}

/**
 * Add Flow source to the [MediatorLiveData] that just forwards its value to MediatorLiveData's value.
 *
 * This should only be used when [T] is not [Resource] and thus [addResourceSource] cannot be used.
 *
 * @param timeoutInMs The timeout duration before cancelling the block if
 * there are no active observers
 */
fun <T> MediatorLiveData<T>.addSource(source: Flow<T>, timeoutInMs: Long = DEFAULT_TIMEOUT) {
    addSource(source, timeoutInMs) { value = it }
}

/**
 * Add [Resource] Flow source to the [Resource][Resource] [MediatorLiveData].
 *
 * Terminating errors from from this flow will be emitted as [Resource.Error].
 */
fun <T, S> MediatorLiveData<T>.addResourceSource(
    source: Flow<Resource<S>>,
    timeoutInMs: Long = DEFAULT_TIMEOUT,
    onChanged: (Resource<S>?) -> Unit
) {
    addSource(liveData<Resource<S>?>(timeoutInMs = timeoutInMs) {
        try {
            source.collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }, onChanged)
}

/**
 * Add [Resource] Flow source to the [Resource][Resource] [MediatorLiveData]
 * that just forwards its value to MediatorLiveData's value.
 *
 * Terminating errors from from this flow will be emitted as [Resource.Error].
 */
suspend fun <T> MediatorLiveData<Resource<T>>.addResourceSource(
    source: Flow<Resource<T>>,
    timeoutInMs: Long = DEFAULT_TIMEOUT
) {
    addResourceSource(source, timeoutInMs) { value = it }
}

/**
 * Collect all values from this flow into target LiveData.
 *
 * This method suspends until flow is terminated.
 */
suspend fun <T> Flow<T>.collectInto(liveData: MutableLiveData<T>) {
    collect {
        liveData.postValue(it)
    }
}

/**
 * Collect all values from this flow into target LiveData.
 *
 * This method suspends until flow is terminated.
 */
suspend fun <T> Flow<Resource<T>>.collectInto(liveData: ResourceLiveData<T>) {
    collect {
        liveData.sendValue(it)
    }
}

internal const val DEFAULT_TIMEOUT = 0L
