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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.testing.ImmediateDispatcherRule
import si.inova.kotlinova.testing.ListObserver
import si.inova.kotlinova.utils.awaitCancellation
import java.util.concurrent.atomic.AtomicBoolean

class LiveDataFlowsTest {
    @get:Rule
    val executor = InstantTaskExecutorRule()

    @get:Rule
    val dispatcher = ImmediateDispatcherRule()

    @Test
    fun receiveValuesFromFlow() = runBlocking<Unit> {
        val flow = flow {
            emit(1)
            emit(2)
            emit(3)
        }

        val mediator = MediatorLiveData<Int>()
        val observer = ListObserver<Int>()

        mediator.observeForever(observer)

        mediator.addSource(flow)

        assertThat(observer.receivedItems).containsExactly(1, 2, 3)
    }

    @Test
    fun receiveValuesFromFlowWithModifier() = runBlocking<Unit> {
        val flow = flow {
            emit(1)
            emit(2)
            emit(3)
        }

        val mediator = MediatorLiveData<Int>()
        val observer = ListObserver<Int>()

        mediator.observeForever(observer)

        mediator.addSource(flow) {
            mediator.value = it!! * 2
        }

        assertThat(observer.receivedItems).containsExactly(2, 4, 6)
    }

    @Test
    fun startObservingFlowAfterObserverStarts() = runBlocking<Unit> {
        val observing = AtomicBoolean(false)

        val flow = flow<Unit> {
            observing.set(true)
            awaitCancellation()
        }

        val mediator = MediatorLiveData<Unit>()
        val observer = ListObserver<Unit>()

        mediator.addSource(flow)

        assertThat(observing.get()).isFalse()

        mediator.observeForever(observer)

        assertThat(observing.get()).isTrue()
    }

    @Test
    fun stopObservingFlowAfterObserverStops() = runBlocking<Unit> {
        val observing = AtomicBoolean(true)

        val flow = flow<Unit> {
            try {
                awaitCancellation()
            } finally {
                observing.set(false)
            }
        }

        val mediator = MediatorLiveData<Unit>()
        val observer = ListObserver<Unit>()

        mediator.addSource(flow)

        mediator.observeForever(observer)
        mediator.removeObserver(observer)

        assertThat(observing.get()).isFalse()
    }

    @Test
    fun reportCrashesFromResourceSourceAsErrorResource() = runBlocking<Unit> {
        val exception = CloneNotSupportedException("ABC")

        val flow = flow<Resource<Int>> {
            emit(Resource.Success(1))
            emit(Resource.Loading(2))

            throw exception
        }

        val mediator = MediatorLiveData<Resource<Int>>()
        val observer = ListObserver<Resource<Int>>()

        mediator.observeForever(observer)

        mediator.addResourceSource(flow)

        assertThat(observer.receivedItems).containsExactly(
            Resource.Success(1),
            Resource.Loading(2),
            Resource.Error(exception)
        )
    }

    @Test
    fun collectIntoLiveData() = runBlocking<Unit> {
        val flow = flow {
            emit(1)
            emit(2)
            emit(3)
        }

        val mediator = MutableLiveData<Int>()
        val observer = ListObserver<Int>()

        mediator.observeForever(observer)

        flow.collectInto(mediator)

        assertThat(observer.receivedItems).containsExactly(1, 2, 3)
    }

    @Test
    fun collectIntoResourceLiveData() = runBlocking<Unit> {
        val flow = flow {
            emit(Resource.Success(1))
            emit(Resource.Loading(2))
            emit(Resource.Success(3))
        }

        val mediator = ResourceLiveData<Int>()
        val observer = ListObserver<Resource<Int>>()

        mediator.observeForever(observer)

        flow.collectInto(mediator)

        assertThat(observer.receivedItems).containsExactly(
            Resource.Success(1),
            Resource.Loading(2),
            Resource.Success(3)
        )
    }

}
