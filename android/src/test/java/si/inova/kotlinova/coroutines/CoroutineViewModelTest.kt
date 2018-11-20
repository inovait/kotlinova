package si.inova.kotlinova.coroutines

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
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
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.testing.CoroutinesTimeMachine
import si.inova.kotlinova.testing.assertIs
import si.inova.kotlinova.utils.addSource

/**
 * @author Matej Drobnic
 */
class CoroutineViewModelTest {
    private lateinit var testViewModel: TestViewModel

    @get:Rule
    val dispatcher = CoroutinesTimeMachine()
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
        testViewModel.firstTask()
        testViewModel.failJob()

        dispatcher.advanceTime(100)

        assertTrue(testViewModel.isResourceTakenPublic(testViewModel.resourceA))
    }

    private class TestViewModel : CoroutineViewModel() {
        val resourceA = ResourceLiveData<Int>()

        fun firstTask() = launchResourceControlTask(resourceA) {
            delay(500)
            sendValue(Resource.Success(10))
        }

        fun secondTask() = launchResourceControlTask(resourceA) {
            delay(500)
            sendValue(Resource.Success(20))
        }

        fun exceptionTask() = launchResourceControlTask(resourceA) {
            delay(500)
            throw IllegalStateException("Test exception")
        }

        fun failJob() = launch {
            throw IllegalStateException("Test exception")
        }

        fun <T> isResourceTakenPublic(resource: MutableLiveData<Resource<T>>): Boolean {
            return super.isResourceTaken(resource)
        }

        fun <T> cancelResourcePublic(resource: MutableLiveData<Resource<T>>) {
            super.cancelResource(resource)
        }

        fun <T> getJob(resource: MutableLiveData<Resource<T>>): Job? {
            @Suppress("USELESS_CAST")
            return activeJobs[resource as MutableLiveData<*>]
        }
    }
}