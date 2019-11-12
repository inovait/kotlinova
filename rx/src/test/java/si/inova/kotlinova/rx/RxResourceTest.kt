package si.inova.kotlinova.rx

import io.reactivex.processors.BehaviorProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import si.inova.kotlinova.data.resources.Resource

@Suppress("EXPERIMENTAL_API_USAGE")
class RxResourceTest {
    @Test
    fun awaitFirstUnpackedValue() = runBlocking {
        val processor = BehaviorProcessor.create<Resource<Int>>()

        val asyncValue = async(Dispatchers.Unconfined) {
            processor.awaitFirstUnpacked()
        }

        processor.offer(Resource.Success(1337))

        assertEquals(1337, asyncValue.await())
    }

    @Test(expected = CloneNotSupportedException::class)
    fun awaitFirstUnpackedError() = runBlocking<Unit> {
        val processor = BehaviorProcessor.create<Resource<Int>>()

        val asyncValue = async(Dispatchers.Unconfined) {
            processor.awaitFirstUnpacked()
        }

        processor.offer(Resource.Error(CloneNotSupportedException()))
        asyncValue.await()
    }
}