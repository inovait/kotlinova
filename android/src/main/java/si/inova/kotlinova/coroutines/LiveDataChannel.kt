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

package si.inova.kotlinova.coroutines

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import si.inova.kotlinova.utils.runOnUiThread

/**
 * Class that transforms [LiveData] into suspendable [Channel].
 *
 * After use it must be disposed with [close] method.
 *
 * @author Matej Drobnic
 */
class LiveDataChannel<T>(private val liveData: LiveData<T>) {
    val channel = Channel<T?>(capacity = Channel.CONFLATED)

    init {
        @Suppress("EXPERIMENTAL_API_USAGE")
        channel.invokeOnClose {
            runOnUiThread {
                liveData.removeObserver(observer)
            }
        }
    }

    private val observer = Observer<T> {
        try {
            channel.offer(it)
        } catch (e: ClosedSendChannelException) {
        }
    }

    init {
        runOnUiThread {
            liveData.observeForever(observer)
        }
    }
}

fun <T> LiveData<T>.toChannel(): ReceiveChannel<T?> {
    return LiveDataChannel(this).channel
}
