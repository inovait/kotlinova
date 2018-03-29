package si.inova.kotlinova.testing

import android.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.experimental.Unconfined
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.dispatcherOverride

/**
 * Rule that converts all thread dispatchers into immediate execution ones.
 *
 * @author Matej Drobnic
 */
class ImmediateDispatcherRule : InstantTaskExecutorRule() {
    override fun finished(description: Description?) {
        super.finished(description)

        dispatcherOverride = { it() }
    }

    override fun starting(description: Description?) {
        super.starting(description)

        dispatcherOverride = { Unconfined }
    }
}