package si.inova.kotlinova.coroutines

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ClosedSendChannelException
import kotlinx.coroutines.experimental.channels.ReceiveChannel
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
