package si.inova.kotlinova.coroutines

import android.arch.lifecycle.MutableLiveData
import android.support.test.espresso.Espresso
import android.support.test.runner.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.testing.awaitSuccessAndThrowErrors
import si.inova.kotlinova.testing.waitUntil
import si.inova.kotlinova.utils.addSource
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CoroutineViewModelAndroidTest {
    private lateinit var testViewModel: TestViewModel

    @Before
    fun setUp() {
        testViewModel = TestViewModel()
    }

    @Test(timeout = 2_000)
    fun unregisterAllResourceSourcesOnNewTask() = runBlocking {
        testViewModel.launchWithNewSource()
        testViewModel.testResource.awaitSuccessAndThrowErrors()

        testViewModel.launchWithoutAddingAnything()
        testViewModel.launchFinished.await()

        assertFalse(testViewModel.testResource.hasAnySources())
    }

    @Test
    fun unregisterResourceOnError() = runBlocking {
        repeat(100) { _ ->
            withTimeout(TimeUnit.SECONDS.toMillis(1), {
                testViewModel = TestViewModel()

                testViewModel.launchWithNewSourceAndCrash()
                testViewModel.testResource.waitUntil { it is Resource.Error }.await()

                testViewModel.launchFinished.await()
                Espresso.onIdle()

                assertTrue(
                    (testViewModel.testResource.value as Resource.Error).exception is IOException
                )
                assertFalse(testViewModel.testResource.hasAnySources())
            })
        }
    }

    private class TestViewModel : CoroutineViewModel() {
        val testResource = ResourceLiveData<Int>()
        val launchFinished = CountDownLatch(1)

        fun launchWithoutAddingAnything() = launchResourceControlTask(testResource) {
            launchFinished.countDown()
        }

        fun launchWithNewSource() = launchResourceControlTask(testResource) {
            addSource(TEST_CONTAINER_LIVE_DATA)
        }

        fun launchWithNewSourceAndCrash() = launchResourceControlTask(testResource) {
            try {
                addSource(TEST_CONTAINER_LIVE_DATA)

                while (!hasAnySources()) {
                    yield()
                }

                throw IOException()
            } finally {
                launchFinished.countDown()
            }
        }
    }
}

private val TEST_CONTAINER_LIVE_DATA =
    MutableLiveData<Resource<Int>>().apply { postValue(Resource.Success(5)) }