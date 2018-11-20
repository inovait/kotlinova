package si.inova.kotlinova.testing

import kotlinx.coroutines.Dispatchers
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.coroutines.TestableDispatchers

/**
 * Rule that converts all thread dispatchers into UI dispatcher that runs under Robolectric.
 *
 * @author Matej Drobnic
 */
class RobolectricDispatcherRule : TestWatcher() {
    override fun finished(description: Description?) {
        super.finished(description)

        TestableDispatchers.dispatcherOverride = { it() }
    }

    override fun starting(description: Description?) {
        super.starting(description)

        TestableDispatchers.dispatcherOverride = { Dispatchers.Main }
    }
}