package si.inova.kotlinova.rx.coroutines

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit

class SingleCoroutinesTest {
    @get:Rule
    val rule = Timeout(1, TimeUnit.SECONDS)

    @Test
    fun awaitSingle() = runBlocking {
        val single = Single.just(32)

        assertEquals(32, single.await())
    }

    @Test(expected = IllegalStateException::class)
    fun awaitSingleThrowErrors() = runBlocking<Unit> {
        val single = Single.error<Int>(IllegalStateException())

        single.await()
    }

    @Test
    fun awaitSingleValueArrivingWhileWaiting() = runBlocking {
        val subject = PublishSubject.create<Int>()
        val single = subject.firstOrError()

        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val awaitTask = async(Dispatchers.Unconfined) {
            single.await()
        }

        assertFalse(awaitTask.isCompleted)
        assertTrue(subject.hasObservers())
        subject.onNext(12)

        assertTrue(awaitTask.isCompleted)
        assertEquals(12, awaitTask.await())
    }

    @Test
    fun awaitSingleUnsubscribeWhenCancellingTask() = runBlocking {
        val subject = PublishSubject.create<Int>()
        val single = subject.firstOrError()

        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val awaitTask = async(Dispatchers.Unconfined) {
            single.await()
        }

        assertFalse(awaitTask.isCompleted)
        assertTrue(subject.hasObservers())

        awaitTask.cancel()
        assertFalse(subject.hasObservers())
    }
}