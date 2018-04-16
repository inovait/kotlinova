package si.inova.kotlinova.coroutines

import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.experimental.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.data.ExtendedMediatorLiveData
import si.inova.kotlinova.data.Resource
import si.inova.kotlinova.testing.TimedDispatcher
import si.inova.kotlinova.testing.assertIs
import si.inova.kotlinova.utils.addSource

/**
 * @author Matej Drobnic
 */
class CoroutineViewModelTest {
    private lateinit var testViewModel: TestViewModel

    @get:Rule
    val dispatcher = TimedDispatcher()

    @Before
    fun setUp() {
        testViewModel = TestViewModel()
    }

    @Test
    fun acquireResourceSuccess() {
        testViewModel.firstTask()

        assertIs(testViewModel.resourceA.value, Resource.Loading::class.java)
        dispatcher.advanceTime(600)
        assertEquals(Resource.Success(10), testViewModel.resourceA.value)
    }

    @Test
    fun acquireResourceError() {
        testViewModel.exceptionTask()

        assertIs(testViewModel.resourceA.value, Resource.Loading::class.java)
        dispatcher.advanceTime(600)
        assertIs(testViewModel.resourceA.value, Resource.Error::class.java)

        val exception = (testViewModel.resourceA.value as Resource.Error<Int>).exception
        assertIs(exception, IllegalStateException::class.java)
        assertEquals("Test exception", exception.message)
    }

    @Test
    fun cancelConcurrentJobs() {
        val firstTaskJob = testViewModel.firstTask()
        val secondTaskJob = testViewModel.secondTask()

        assertTrue(firstTaskJob.isCancelled)
        assertFalse(secondTaskJob.isCancelled)

        dispatcher.advanceTime(600)
        assertEquals(Resource.Success(20), testViewModel.resourceA.value)
    }

    @Test
    fun doNotCancelConcurrentJobsWithNoUniqueFlag() {
        val firstTaskJob = testViewModel.firstNonUniqueTask()
        val secondTaskJob = testViewModel.secondNonUniqueTask()

        assertFalse(firstTaskJob.isCancelled)
        assertFalse(secondTaskJob.isCancelled)

        dispatcher.advanceTime(600)
        assertEquals(Resource.Success(15), testViewModel.resourceA.value)
        dispatcher.advanceTime(600)
        assertEquals(Resource.Success(25), testViewModel.resourceA.value)
    }

    @Test
    fun cancelMediatorLiveDataSubscriptionsWhenCancellingJob() {
        testViewModel.resourceA.observeForever {}

        val additionalResource = MutableLiveData<Resource<Int>>()
        testViewModel.resourceA.addSource(additionalResource)
        assertTrue(additionalResource.hasActiveObservers())

        testViewModel.firstTask()

        assertFalse(additionalResource.hasActiveObservers())
    }

    @Test
    fun isResourceTaken() {
        assertFalse(testViewModel.isResourceTakenPublic(testViewModel.resourceA))

        testViewModel.firstTask()

        assertTrue(testViewModel.isResourceTakenPublic(testViewModel.resourceA))
        dispatcher.advanceTime(600)
        assertFalse(testViewModel.isResourceTakenPublic(testViewModel.resourceA))
    }

    @Test
    fun cancelResource() {
        assertFalse(testViewModel.cancelResourcePublic(testViewModel.resourceA))

        testViewModel.firstTask()

        assertTrue(testViewModel.cancelResourcePublic(testViewModel.resourceA))

        assertIs(testViewModel.resourceA.value, Resource.Cancelled::class.java)
        dispatcher.advanceTime(600)
        assertIs(testViewModel.resourceA.value, Resource.Cancelled::class.java)
    }

    @Test
    fun doNotCancelMediatorLiveDataSubscriptionsWithNonUniqueJobs() {
        testViewModel.resourceA.observeForever {}

        val additionalResource = MutableLiveData<Resource<Int>>()
        testViewModel.resourceA.addSource(additionalResource)
        assertTrue(additionalResource.hasActiveObservers())

        testViewModel.firstNonUniqueTask()

        assertTrue(additionalResource.hasActiveObservers())
    }

    private class TestViewModel : CoroutineViewModel() {
        val resourceA = ExtendedMediatorLiveData<Resource<Int>>()

        fun firstTask() = launchManaged {
            acquireResource(resourceA) { output ->
                delay(500)
                output.postValue(Resource.Success(10))
            }
        }

        fun secondTask() = launchManaged {
            acquireResource(resourceA) { output ->
                delay(500)
                output.postValue(Resource.Success(20))
            }
        }

        fun firstNonUniqueTask() = launchManaged {
            acquireResource(resourceA, unique = false) { output ->
                delay(500)
                output.postValue(Resource.Success(15))
            }
        }

        fun secondNonUniqueTask() = launchManaged {
            acquireResource(resourceA, unique = false) { output ->
                delay(700)
                output.postValue(Resource.Success(25))
            }
        }

        fun exceptionTask() = launchManaged {
            acquireResource(resourceA) { output ->
                delay(500)
                throw IllegalStateException("Test exception")
            }
        }

        fun <T> isResourceTakenPublic(resource: MutableLiveData<Resource<T>>): Boolean {
            return super.isResourceTaken(resource)
        }

        fun <T> cancelResourcePublic(resource: MutableLiveData<Resource<T>>): Boolean {
            return super.cancelResource(resource)
        }
    }
}