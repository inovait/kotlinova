package si.inova.kotlinova.data

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.only
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.CoroutinesTimeMachine

/**
 * @author Matej Drobnic
 */
class LastResultAsyncItemProcessorTest {
    private lateinit var processorLastResult: LastResultAsyncItemProcessor<Int, Int>

    @get:Rule()
    val dispatcher = CoroutinesTimeMachine()

    @Before
    fun setUp() {
        processorLastResult = LastResultAsyncItemProcessor()
    }

    @Test
    fun simpleProcess() {
        val observer: Function1<Int, Unit> = mock()

        processorLastResult.process(10, observer) {
            it * 2
        }

        dispatcher.triggerActions()

        verify(observer, only()).invoke(20)
    }

    @Test
    fun multipleInvokes() {
        val observer: Function1<Int, Unit> = mock()
        val process: suspend CoroutineScope.(Int) -> Int = {
            delay(100)
            it * 2
        }

        dispatcher.triggerActions()

        processorLastResult.process(1, observer, process)
        processorLastResult.process(2, observer, process)
        processorLastResult.process(3, observer, process)
        processorLastResult.process(4, observer, process)
        processorLastResult.process(5, observer, process)

        verifyZeroInteractions(observer)
        dispatcher.advanceTime(100)
        verify(observer, only()).invoke(10)
    }
}