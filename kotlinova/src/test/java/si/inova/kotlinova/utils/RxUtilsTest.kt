package si.inova.kotlinova.utils

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import io.github.plastix.rxschedulerrule.RxSchedulerRule
import io.reactivex.Flowable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import org.junit.Rule
import org.junit.Test
import org.reactivestreams.Subscription
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule

/**
 * @author Matej Drobnic
 */
class RxUtilsTest {
    @get:Rule
    val rxRule = RxSchedulerRule()
    @get:Rule
    val exceptionsRule = UncaughtExceptionThrowRule()

    @Test
    fun use() {
        val onSubscribe: Consumer<in Subscription> = mock()
        val onDispose: Action = mock()
        val action: () -> Unit = mock()

        val flowable = Flowable.never<Unit>()
            .doOnSubscribe(onSubscribe)
            .doFinally(onDispose)

        inOrder(onSubscribe, action, onDispose) {
            verifyNoMoreInteractions()

            flowable.use(action)

            verify(onSubscribe).accept(any())
            verify(action).invoke()
            verify(onDispose).run()

            verifyNoMoreInteractions()
        }
    }
}