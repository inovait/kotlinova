package si.inova.kotlinova.testing

import io.reactivex.Scheduler
import io.reactivex.functions.Function
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.Callable

/**
 * JUnit Test Rule which overrides RxJava and Android schedulers for use in unit tests.
 *
 *
 * All schedulers are replaced with Schedulers.trampoline().
 *
 *
 *
 * @author https://github.com/Plastix/RxSchedulerRule/blob/master/rx2/src/main/java/
 * io/github/plastix/rxschedulerrule/RxSchedulerRule.java, Adapted by Matej Drobnic
 */
class RxSchedulerRule(private val scheduler: Scheduler = Schedulers.trampoline()) : TestRule {
    private val schedulerFunction = Function<Scheduler, Scheduler> { scheduler }

    private val schedulerFunctionLazy =
            Function<Callable<Scheduler>, Scheduler> { scheduler }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxJavaPlugins.reset()
                RxJavaPlugins.setIoSchedulerHandler(schedulerFunction)
                RxJavaPlugins.setNewThreadSchedulerHandler(schedulerFunction)
                RxJavaPlugins.setComputationSchedulerHandler(schedulerFunction)

                base.evaluate()
                RxJavaPlugins.reset()
            }
        }
    }
}