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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Flowable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.RuleChain
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.testing.CoroutinesTimeMachine
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import si.inova.kotlinova.testing.assertIs
import si.inova.kotlinova.utils.addResourceSource
import si.inova.kotlinova.utils.addSource

/**
 * @author Matej Drobnic
 */
class CoroutineViewModelTest {
    private lateinit var testViewModel: TestViewModel

    @get:Rule
    val dispatcher = CoroutinesTimeMachine()

    val expectException = ExpectedException.none()

    @get:Rule
    val exceptionsAndUncaughtRule =
        RuleChain.outerRule(expectException).around(UncaughtExceptionThrowRule())

    @get:Rule
    val archRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        testViewModel = TestViewModel()
    }

    @Test
    fun acquireResourceSuccess() {
        testViewModel.firstTask()
        dispatcher.triggerActions()

        assertIs(testViewModel.resourceA.value, Resource.Loading::class.java)
        dispatcher.advanceTime(600)
        assertEquals(Resource.Success(10), testViewModel.resourceA.value)
    }

    @Test
    fun acquireResourceError() {
        testViewModel.exceptionTask()
        dispatcher.triggerActions()

        assertIs(testViewModel.resourceA.value, Resource.Loading::class.java)
        dispatcher.advanceTime(600)
        assertIs(testViewModel.resourceA.value, Resource.Error::class.java)

        val exception = (testViewModel.resourceA.value as Resource.Error<Int>).exception
        assertIs(exception, IllegalStateException::class.java)
        assertEquals("Test exception", exception.message)
    }

    @Test
    fun cancelConcurrentJobs() {
        testViewModel.firstTask()
        dispatcher.triggerActions()
        val firstTaskJob = testViewModel.getJob(testViewModel.resourceA)

        testViewModel.secondTask()
        dispatcher.triggerActions()
        val secondTaskJob = testViewModel.getJob(testViewModel.resourceA)

        assertNotSame(firstTaskJob, secondTaskJob)
        assertNotNull(firstTaskJob)
        assertNotNull(secondTaskJob)

        assertTrue(firstTaskJob!!.isCancelled)
        assertFalse(secondTaskJob!!.isCancelled)

        dispatcher.advanceTime(600)
        assertEquals(Resource.Success(20), testViewModel.resourceA.value)
    }

    @Test
    fun cancelMediatorLiveDataSubscriptionsWhenCancellingJob() {
        testViewModel.resourceA.observeForever {}

        val additionalResource = MutableLiveData<Resource<Int>>()
        testViewModel.resourceA.addSource(additionalResource)
        assertTrue(additionalResource.hasActiveObservers())

        testViewModel.firstTask()
        dispatcher.triggerActions()

        assertFalse(additionalResource.hasActiveObservers())
    }

    @Test
    fun isResourceTaken() {
        assertFalse(testViewModel.isResourceTakenPublic(testViewModel.resourceA))

        testViewModel.firstTask()
        dispatcher.triggerActions()

        assertTrue(testViewModel.isResourceTakenPublic(testViewModel.resourceA))
        dispatcher.advanceTime(600)
        assertFalse(testViewModel.isResourceTakenPublic(testViewModel.resourceA))
    }

    @Test
    fun cancelResource() {
        testViewModel.firstTask()
        dispatcher.triggerActions()

        testViewModel.cancelResourcePublic(testViewModel.resourceA)
        dispatcher.triggerActions()

        assertIs(testViewModel.resourceA.value, Resource.Cancelled::class.java)
        dispatcher.advanceTime(600)
        assertIs(testViewModel.resourceA.value, Resource.Cancelled::class.java)
    }

    @Test
    fun concurrentLaunches() {
        repeat(1000) {
            testViewModel.firstTask()
            dispatcher.triggerActions()
        }
    }

    @Test
    fun `Do not cancel all jobs on single job failure`() {
        expectException.expectMessage("Test exception")

        testViewModel.firstTask()
        testViewModel.failJob()

        dispatcher.advanceTime(100)

        assertTrue(testViewModel.isResourceTakenPublic(testViewModel.resourceA))
    }

    @Test
    fun `Receive errors from common error observer when redirection is enabled by default`() {
        val commonErrorViewModel = TestCommonErrorViewModel()

        val errorObserver: Observer<Throwable> = mock()
        commonErrorViewModel.errors.observeForever(errorObserver)

        commonErrorViewModel.exceptionTask()
        dispatcher.triggerActions()

        assertIs(commonErrorViewModel.resourceA.value, Resource.Cancelled::class.java)
        verify(errorObserver).onChanged(argThat { this is IllegalStateException })
    }

    @Test
    fun `Also redirect received errors to common error observer`() {
        val commonErrorViewModel = TestCommonErrorViewModel()
        commonErrorViewModel.resourceA.observeForever {}

        val errorObserver: Observer<Throwable> = mock()
        commonErrorViewModel.errors.observeForever(errorObserver)

        commonErrorViewModel.exceptionPassingTask()
        dispatcher.triggerActions()

        assertIs(commonErrorViewModel.resourceA.value, Resource.Cancelled::class.java)
        verify(errorObserver).onChanged(argThat { this is IllegalStateException })
    }

    @Test
    fun `Receive errors from common error observer when redirection is enabled by specific job`() {
        val errorObserver: Observer<Throwable> = mock()
        testViewModel.errors.observeForever(errorObserver)

        testViewModel.exceptionTaskWithCommonError()
        dispatcher.triggerActions()

        assertIs(testViewModel.resourceA.value, Resource.Cancelled::class.java)
        verify(errorObserver).onChanged(argThat { this is IllegalStateException })
    }

    @Test
    fun `Crash when common error observer is not being observed by anyone`() {
        expectException.expect(LiveDataCoroutineResourceManager.UndeliverableException::class.java)

        val commonErrorViewModel = TestCommonErrorViewModel()

        commonErrorViewModel.exceptionTask()
        dispatcher.triggerActions()

        assertIs(commonErrorViewModel.resourceA.value, Resource.Cancelled::class.java)
    }

    private class TestViewModel : CoroutineViewModel() {
        val resourceA = ResourceLiveData<Int>()

        fun firstTask() = resources.launchResourceControlTask(resourceA) {
            delay(500)
            sendValue(Resource.Success(10))
        }

        fun secondTask() = resources.launchResourceControlTask(resourceA) {
            delay(500)
            sendValue(Resource.Success(20))
        }

        fun exceptionTask() {
            resources.launchResourceControlTask(resourceA) {
                delay(500)
                throw IllegalStateException("Test exception")
            }
        }

        fun exceptionTaskWithCommonError() {
            resources.launchResourceControlTask(
                resourceA,
                routeErrorsToCommonObservable = true
            ) {
                throw IllegalStateException("Test exception")
            }
        }

        fun failJob() = launch {
            throw IllegalStateException("Test exception")
        }

        fun <T> isResourceTakenPublic(resource: MutableLiveData<Resource<T>>): Boolean {
            return resources.isResourceTaken(resource)
        }

        fun <T> cancelResourcePublic(resource: MutableLiveData<Resource<T>>) {
            resources.cancelResource(resource)
        }

        fun <T> getJob(resource: MutableLiveData<Resource<T>>): Job? {
            return resources.getCurrentJob(resource as MutableLiveData<*>)
        }
    }

    private class TestCommonErrorViewModel : CommonErrorCoroutineViewModel() {
        val resourceA = ResourceLiveData<Int>()

        fun exceptionTask() = resources.launchResourceControlTask(resourceA) {
            throw IllegalStateException("Test exception")
        }

        fun exceptionPassingTask() = resources.launchResourceControlTask(resourceA) {
            addResourceSource(Flowable.error(IllegalStateException("Test exception")))
        }
    }
}