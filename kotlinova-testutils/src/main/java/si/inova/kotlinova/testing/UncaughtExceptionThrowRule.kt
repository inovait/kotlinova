package si.inova.kotlinova.testing

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.MultipleFailureException
import java.util.Collections

/**
 * Test rule that fails any test that have thrown any test exceptions in different threads
 *
 * @author Matej Drobnic
 */
class UncaughtExceptionThrowRule : TestWatcher() {
    var oldExceptionHandler: Thread.UncaughtExceptionHandler? = null

    val errors = Collections.synchronizedList(ArrayList<Throwable>())

    override fun finished(description: Description?) {
        Thread.setDefaultUncaughtExceptionHandler(oldExceptionHandler)
        MultipleFailureException.assertEmpty(errors)
    }

    override fun starting(description: Description?) {
        oldExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler({ _, e -> errors.add(e) })
    }
}