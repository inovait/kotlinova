package si.inova.kotlinova.rx.coroutines

import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit

class CompletableCoroutinesTest {
    @get:Rule
    val rule = Timeout(1, TimeUnit.SECONDS)

    @Test
    fun awaitCompletable() = runBlocking {
        val subject = PublishSubject.create<Int>()
        val completable = subject.ignoreElements()

        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val awaitTask = async(Dispatchers.Unconfined) {
            completable.await()
        }

        assertFalse(awaitTask.isCompleted)
        assertTrue(subject.hasObservers())
        subject.onComplete()

        assertTrue(awaitTask.isCompleted)
    }

    @Test(expected = IllegalStateException::class)
    fun awaitCompletableThrowErrors() = runBlocking<Unit> {
        val completable = Completable.error(IllegalStateException())

        completable.await()
    }

    @Test
    fun awaitCompletableUnsubscribeWhenCancellingTask() = runBlocking {
        val subject = PublishSubject.create<Int>()
        val completable = subject.ignoreElements()

        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val awaitTask = async(Dispatchers.Unconfined) {
            completable.await()
        }

        assertFalse(awaitTask.isCompleted)
        assertTrue(subject.hasObservers())

        awaitTask.cancel()
        assertFalse(subject.hasObservers())
    }
}