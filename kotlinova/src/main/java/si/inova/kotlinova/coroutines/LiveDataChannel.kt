package si.inova.kotlinova.coroutines

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import si.inova.kotlinova.utils.runOnUiThread

/**
 * Class that transforms [LiveData] into suspendable [Channel].
 *
 * After use it must be disposed with [close] method.
 *
 * @author Matej Drobnic
 */
class LiveDataChannel<T>(private val liveData: LiveData<T>) : ConflatedChannel<T?>(),
    ReceiveChannel<T?> {
    private val observer = Observer<T> {
        if (!isClosedForSend) {
            offer(it)
        }
    }

    init {
        runOnUiThread {
            liveData.observeForever(observer)
        }
    }

    override fun afterClose(cause: Throwable?) {
        super.afterClose(cause)

        runOnUiThread {
            liveData.removeObserver(observer)
        }
    }
}

fun <T> LiveData<T>.toChannel(): ReceiveChannel<T?> {
    return LiveDataChannel(this)
}
