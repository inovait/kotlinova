package si.inova.kotlinova.rx.coroutines

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.*
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

        val awaitTask = async(Unconfined) {
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

        val awaitTask = async(Unconfined) {
            single.await()
        }

        assertFalse(awaitTask.isCompleted)
        assertTrue(subject.hasObservers())

        awaitTask.cancel()
        assertFalse(subject.hasObservers())
    }
}