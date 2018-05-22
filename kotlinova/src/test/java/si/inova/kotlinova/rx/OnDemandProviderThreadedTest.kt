package si.inova.kotlinova.rx

import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.delay
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    private open class TestProvider
        : OnDemandProvider<Unit>(rxScheduler = Schedulers.trampoline()) {
        private val currentlyActive = AtomicBoolean(false)

        override suspend fun CoroutineScope.onActive() {
            // currentlyActive test currently fails due to bug in coroutines:
            // https://github.com/Kotlin/kotlinx.coroutines/issues/357
            // minor issue, keep commented until fix is found

            //assertFalse(currentlyActive.getAndSet(true))
            assertTrue(this@TestProvider.isActive)
            assertTrue(isActive)

            delay(500)
        }

        override fun onInactive() {
            //assertTrue(currentlyActive.getAndSet(false))
            assertFalse(this@TestProvider.isActive)
        }
    }

    private class TestProviderNoDebounce : TestProvider() {
        init {
            debounceTimeout = 0
        }
    }
}