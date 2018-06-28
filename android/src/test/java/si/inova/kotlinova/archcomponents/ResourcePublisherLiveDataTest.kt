package si.inova.kotlinova.archcomponents

import android.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.testing.ImmediateDispatcherRule

/**
 * @author Matej Drobnic
 */
class ResourcePublisherLiveDataTest {
    @get:Rule
    val rule = ImmediateDispatcherRule()
    @get:Rule
    val archRule = InstantTaskExecutorRule()

    @Test
    fun propagatingValue() {
        val subscriber = Flowable.just<Resource<Int>>(Resource.Success(10))

        val liveData = ResourcePublisherLiveData(subscriber)
        liveData.observeForever { }

        assertEquals(Resource.Success(10), liveData.value)
    }

    @Test
    fun propagatingErrors() {
        val error = RuntimeException("Test")
        val subscriber = Flowable.error<Resource<Int>>(error)

        val liveData = ResourcePublisherLiveData(subscriber)
        liveData.observeForever { }

        assertEquals(Resource.Error<Int>(error), liveData.value)
    }
}