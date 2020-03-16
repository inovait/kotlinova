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

import kotlinx.coroutines.*
import si.inova.kotlinova.coroutines.TestableDispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Class that helps with processing stream of items asynchronously.
 *
 * Callback is always called only for the last item
 * (e.g. even if you call process() 10000 times, it will only receive callback for last result)
 *
 * @author Matej Drobnic
 */
class LastResultAsyncItemProcessor<I, O>(
        private val processingContext: CoroutineContext = TestableDispatchers.Default,
        private val callbackContext: CoroutineContext = Dispatchers.Main
) {
    @Volatile
    private var lastJob: Job? = null

    fun process(input: I, callback: (O) -> Unit, process: suspend CoroutineScope.(I) -> O) {
        lastJob?.cancel()

        lastJob = GlobalScope.launch(callbackContext, CoroutineStart.DEFAULT, {
            val result = withContext(processingContext) {
                process(input)
            }

            if (isActive) {
                callback(result)
            }
        })
    }
}

fun <T> LastResultAsyncItemProcessor<Unit, T>.process(
        callback: (T) -> Unit,
        process: suspend CoroutineScope.(Unit) -> T
) {
    process(Unit, callback, process)
}