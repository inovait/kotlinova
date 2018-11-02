package si.inova.kotlinova.rx

import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.TimberExceptionThrowRule
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Matej Drobnic
 */
class OnDemandProviderThreadedTest {
    @get:Rule
    val exceptionRule = UncaughtExceptionThrowRule()
    @get:Rule
    val timberRule = TimberExceptionThrowRule()

    @Ignore
    @Test(timeout = 10_000)
    fun manySubscribes() {
        // Thread concurrency issues do not reproduce every time
        // repeat it multiple times to increase chance of failure
        repeat(100) {
            val provider = TestProviderNoDebounce()

            repeat(200) {
                provider.flowable.subscribe().dispose()
            }
        }

        ForkJoinPool.commonPool().awaitTermination(5, TimeUnit.DAYS)
    }

    @Test(timeout = 10_000)
    fun manySubscribesWithDebounce() {
        // Thread concurrency issues do not reproduce every time
        // repeat it multiple times to increase chance of failure
        repeat(100) {
            val provider = TestProvider()

            repeat(200) {
                provider.flowable.subscribe().dispose()
            }
        }

        ForkJoinPool.commonPool().awaitTermination(5, TimeUnit.DAYS)
    }

    private open class TestProvider
        : OnDemandProvider<Unit>(rxScheduler = Schedulers.trampoline()) {
        private val currentlyActive = AtomicBoolean(false)

        override suspend fun CoroutineScope.onActive() {
            assertFalse(currentlyActive.getAndSet(true))
            assertTrue(this@TestProvider.isActive)
            assertTrue(isActive)

            delay(500)
        }

        override fun onInactive() {
            assertTrue(currentlyActive.getAndSet(false))
            assertFalse(this@TestProvider.isActive)
        }
    }

    private class TestProviderNoDebounce : TestProvider() {
        init {
            debounceTimeout = 0
        }
    }
}