package si.inova.kotlinova.coroutines

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

/**
 * Class that transforms [LiveData] into suspendable [Channel].
 *
 * After use it must be disposed with [close] method.
 *
 * @author Matej Drobnic
 */
class LiveDataChannel<T>(private val liveData: LiveData<T>) : ConflatedChannel<T?>(),
    SubscriptionReceiveChannel<T?> {
    private val observer = Observer<T> {
        if (!isClosedForSend) {
            offer(it)
        }
    }

    init {
        liveData.observeForever(observer)
    }

    override fun close() {
        liveData.removeObserver(observer)
    }
}

fun <T> LiveData<T>.toChannel(): SubscriptionReceiveChannel<T?> {
    return LiveDataChannel(this)
}
