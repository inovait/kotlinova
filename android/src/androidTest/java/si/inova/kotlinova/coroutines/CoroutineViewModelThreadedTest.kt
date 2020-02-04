package si.inova.kotlinova.coroutines

import androidx.test.espresso.Espresso
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.yield
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

    @Test(timeout = 30_000)
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

            Espresso.onIdle()
            testViewModel.latch.unlock()
            testViewModel.publicParentJob().children.forEach { it.join() }
            Espresso.onIdle()

            verify(callback).invoke()
            assertIs(testViewModel.resourceA.value, Resource.Success::class.java)
        }
    }

    private class TestViewModel : CoroutineViewModel() {
        val resourceA = ResourceLiveData<Unit>()
        var callback: (() -> Unit)? = null

        val latch = Mutex(true)

        fun testTaskInvokingCallback() = resources.launchResourceControlTask(resourceA) {
            latch.awaitUnlockIfLocked()
            yield()
            callback?.invoke()
            sendValue(Resource.Success(Unit))
        }

        fun testTask() = resources.launchResourceControlTask(resourceA) {
        }

        fun cancelResourceA() {
            resources.cancelResource(resourceA)
        }

        fun publicParentJob(): Job {
            return coroutineContext[Job]!!
        }
    }
}