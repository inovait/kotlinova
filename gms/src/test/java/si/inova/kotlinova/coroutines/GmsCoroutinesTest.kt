package si.inova.kotlinova.coroutines

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Dispatchers.Unconfined
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
class GmsCoroutinesTest {
    @Test
    fun testTaskAwaitResult1() = runBlocking {
        val task = Tasks.forResult("SUCCESS")
        assertEquals("SUCCESS", task.await())
    }

    @Test
    fun testTaskAwaitResult2() = runBlocking {
        val task = TaskCompletionSource<String>()
        val result = async(Dispatchers.Unconfined) { task.task.await() }

        task.setResult("SUCCESS")

        assertEquals("SUCCESS", result.await())
    }

    @Test(expected = NoSuchElementException::class)
    fun testTaskAwaitException1() = runBlocking<Unit> {
        val task = Tasks.forException<String>(NoSuchElementException())

        task.await()
    }

    @Test(expected = NoSuchElementException::class)
    fun testTaskAwaitException2() = runBlocking<Unit> {
        val task = TaskCompletionSource<String>()
        val result = async(Dispatchers.Unconfined) { task.task.await() }

        task.setException(NoSuchElementException())

        result.await()
    }
}