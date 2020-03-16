/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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