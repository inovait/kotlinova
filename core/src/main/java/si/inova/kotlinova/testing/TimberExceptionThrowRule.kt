package si.inova.kotlinova.testing

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.MultipleFailureException
import timber.log.Timber
import java.util.Collections

/**
 * Test rule that fails any test that have thrown any exceptions through Timber logging
 *
 * @author Matej Drobnic
 */
class TimberExceptionThrowRule : TestWatcher() {
    val errors = Collections.synchronizedList(ArrayList<Throwable>())

    private val tree = object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, exception: Throwable?) {
            exception?.let { errors.add(it) }
        }
    }

    override fun finished(description: Description?) {
        Timber.uproot(tree)
        MultipleFailureException.assertEmpty(errors)
    }

    override fun starting(description: Description?) {
        Timber.plant(tree)
    }
}