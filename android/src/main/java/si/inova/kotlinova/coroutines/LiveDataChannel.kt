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
