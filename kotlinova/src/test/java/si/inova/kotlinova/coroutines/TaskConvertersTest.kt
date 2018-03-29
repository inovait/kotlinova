package si.inova.kotlinova.coroutines

import android.arch.lifecycle.MutableLiveData
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import si.inova.kotlinova.utils.map
import si.inova.kotlinova.testing.LocalFunction0

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
class TaskConvertersTest {
    @Test
    fun testTaskAwaitResult1() = runBlocking {
        val task = Tasks.forResult("SUCCESS")
        assertEquals("SUCCESS", task.await())
    }

    @Test
    fun testTaskAwaitResult2() = runBlocking {
        val task = TaskCompletionSource<String>()
        val result = async(Unconfined) { task.task.await() }

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
        val result = async(Unconfined) { task.task.await() }

        task.setException(NoSuchElementException())

        result.await()
    }

    @Test
    fun testAwaitFirstValue() {
        val data = MutableLiveData<Int>()
        assertFalse(data.hasActiveObservers())
        val task = async(UI) { data.awaitFirstValue() }

        assertFalse(task.isCompleted)
        assertTrue(data.hasActiveObservers())

        data.value = 10
        assertTrue(task.isCompleted)
        assertEquals(10, task.getCompleted())
        assertFalse(data.hasActiveObservers())
    }

    @Test
    fun testAwaitFirstValueWithBlock() = runBlocking<Unit> {
        val data = MutableLiveData<Int>()
        val testBlock: LocalFunction0<Unit> = mock()
        data.value = 10
        data.awaitFirstValue(runAfterObserve = testBlock)

        verify(testBlock).invoke()
    }

    @Test
    fun testAwaitFirstValueAllowExisting() {
        val data = MutableLiveData<Int>()

        data.value = 10
        val task = async(UI) { data.awaitFirstValue(ignoreExistingValue = false) }
        data.value = 20

        assertEquals(10, task.getCompleted())
    }

    @Test
    fun testAwaitFirstValueIgnoreExisting() {
        val data = MutableLiveData<Int>()

        data.value = 10
        val task = async(UI) {
            data.awaitFirstValue(ignoreExistingValue = true) {
                data.value = 20
            }
        }

        assertEquals(20, task.getCompleted())
    }

    @Test
    fun testAwaitFirstValueIgnoreExistingThroughMediator() {
        val data = MutableLiveData<Int>()

        // Wrap around MediatorLiveData
        val mappedData = data.map { it!! * 5 }

        data.value = 10
        val task = async(UI) {
            mappedData.awaitFirstValue(ignoreExistingValue = true) {
                data.value = 20
            }
        }

        assertEquals(100, task.getCompleted())
    }

    @Test
    fun testAwaitFirstValueIgnoreExistingSameValue() {
        val data = MutableLiveData<Int>()

        data.value = 10
        val task = async(UI) { data.awaitFirstValue(ignoreExistingValue = true) }
        assertFalse(task.isCompleted)
        data.value = 10

        assertTrue(task.isCompleted)
        assertEquals(10, task.getCompleted())
    }
}