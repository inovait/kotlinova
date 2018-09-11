package si.inova.kotlinova.coroutines

import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import kotlinx.coroutines.experimental.yield
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule

/**
 * @author Matej Drobnic
 */
class LiveDataCoroutinesAndroidTest {
    @get:Rule
    val threadErrorRule = UncaughtExceptionThrowRule()

    @Test
    fun testCancelAwaitFirstValueOnSeparateThread() = runBlocking {
        val data = MutableLiveData<Int>()

        val thread = newSingleThreadContext("Test thread")
        thread.use {
            withContext(thread) {
                val task = async(thread) {
                    data.awaitFirstValue()
                }

                // Block a bit until another thread does the task
                for (i in 0 until 200) {
                    if (data.hasActiveObservers()) {
                        break
                    }

                    yield()
                }

                withContext(UI) {
                    assertTrue(data.hasActiveObservers())
                }

                task.cancel()

                // Block a bit until another thread does the task
                for (i in 0 until 200) {
                    if (!data.hasActiveObservers()) {
                        break
                    }

                    yield()
                }

                withContext(UI) {
                    assertFalse(data.hasActiveObservers())
                }
            }
        }

    }
}