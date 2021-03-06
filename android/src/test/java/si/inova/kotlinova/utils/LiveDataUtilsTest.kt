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

package si.inova.kotlinova.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.data.resources.Resource

@Suppress("EXPERIMENTAL_API_USAGE")
class LiveDataUtilsTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun awaitFirstUnpackedValue() = runBlocking {
        val liveData = MutableLiveData<Resource<Int>>()

        val asyncValue = async(Dispatchers.Unconfined) {
            liveData.awaitFirstUnpacked()
        }

        liveData.value = Resource.Cancelled()
        liveData.value = Resource.Success(1337)

        assertEquals(1337, asyncValue.await())
    }

    @Test(expected = CloneNotSupportedException::class)
    fun awaitFirstUnpackedError() = runBlocking<Unit> {
        val liveData = MutableLiveData<Resource<Int>>()

        val asyncValue = async(Dispatchers.Unconfined) {
            liveData.awaitFirstUnpacked()
        }

        liveData.value = Resource.Loading()
        liveData.value = Resource.Error(CloneNotSupportedException())

        asyncValue.await()
    }
}