package si.inova.kotlinova.testing

import kotlinx.coroutines.delay
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.TestableDispatchers

/**
 * Rule that converts all thread dispatchers into immediate execution ones while also
 * removing any [delay] calls.
 *
 * @author Matej Drobnic
 */
class NoDelayImmediateDispatcherRule : TestWatcher() {
    override fun finished(description: Description?) {
        super.finished(description)

        TestableDispatchers.dispatcherOverride = { it() }
    }

    override fun starting(description: Description?) {
        super.starting(description)

        TestableDispatchers.dispatcherOverride = { NoDelayUnconfinedDispatcher() }
    }
}