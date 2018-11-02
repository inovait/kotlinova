package si.inova.kotlinova.testing

import kotlinx.coroutines.Dispatchers
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.dispatcherOverride

/**
 * Rule that converts all thread dispatchers into immediate execution ones.
 *
 * @author Matej Drobnic
 */
class ImmediateDispatcherRule : TestWatcher() {
    override fun finished(description: Description?) {
        super.finished(description)

        dispatcherOverride = { it() }
    }

    // UNCONFINED is experimental, but it is still fine to use it with tests
    @Suppress("EXPERIMENTAL_API_USAGE")
    override fun starting(description: Description?) {
        super.starting(description)

        dispatcherOverride = { Dispatchers.Unconfined }
    }
}