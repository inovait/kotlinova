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