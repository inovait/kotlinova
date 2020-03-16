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

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
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