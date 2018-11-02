package si.inova.kotlinova.coroutines

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.joinChildren
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.yield
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.ResourceLiveData
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import si.inova.kotlinova.testing.assertIs
import si.inova.kotlinova.utils.awaitUnlockIfLocked

/**
 * @author Matej Drobnic
 */
class CoroutineViewModelThreadedTest {
    @get:Rule
    val exceptionRule = UncaughtExceptionThrowRule()
    @get:Rule
    val liveDataFixRule = InstantTaskExecutorRule()

    @Test(timeout = 10_000)
    fun concurrentLaunches() = runBlocking {
        // Thread concurrency issues do not reproduce every time
        // repeat it multiple times to increase chance of failure

        repeat(500) {
            val testViewModel = TestViewModel()
            val callback: () -> Unit = mock()
            testViewModel.callback = callback

            repeat(100) {
                testViewModel.testTask()
            }

            repeat(100) {
                testViewModel.testTask()
                testViewModel.cancelResourceA()
            }

            repeat(100) {
                testViewModel.testTaskInvokingCallback()
            }

            testViewModel.latch.unlock()
            @Suppress("DEPRECATION")
            testViewModel.publicParentJob().joinChildren()

            verify(callback).invoke()
            assertIs(testViewModel.resourceA.value, Resource.Success::class.java)
        }
    }

    private class TestViewModel : CoroutineViewModel() {
        val resourceA = ResourceLiveData<Unit>()
        var callback: (() -> Unit)? = null

        val latch = Mutex(true)

        fun testTaskInvokingCallback() = launchResourceControlTask(resourceA) {
            latch.awaitUnlockIfLocked()
            yield()
            callback?.invoke()
            sendValue(Resource.Success(Unit))
        }

        fun testTask() = launchResourceControlTask(resourceA) {
        }

        fun cancelResourceA() {
            cancelResource(resourceA)
        }

        fun publicParentJob(): Job {
            return parentJob
        }
    }
}