package si.inova.kotlinova.navigation.kmmsample.util

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


fun BrowserWindow.visibilityChangeEvent(): Flow<BrowserEvent> = eventFlow("visibilitychange")
fun BrowserWindow.unloadEvent(): Flow<BrowserEvent> = eventFlow("unload")

internal fun BrowserWindow.popState(): Flow<BrowserPopStateEvent> = eventFlow("popstate")

@OptIn(DelicateCoroutinesApi::class)
private fun <T: BrowserEvent> BrowserWindow.eventFlow(event: String)  = callbackFlow<T> {
    val localWindow = this@eventFlow

    val callback: (T) -> Unit = { event ->
        if (!isClosedForSend) {
            trySend(event)
        }
    }

    localWindow.addEventListener(event, callback)
    awaitClose { localWindow.removeEventListener(event, callback) }
}

external interface BrowserLocation {
    val origin: String
    val pathname: String
    val hash: String
    val search: String
}

external interface BrowserHistory {
    val state: String?

    fun pushState(data: String?, title: String, url: String?)

    fun replaceState(data: String?, title: String, url: String?)

    fun back()
}

external interface BrowserEvent {
}

internal external interface BrowserPopStateEvent : BrowserEvent {
    val state: String?
}

external interface BrowserEventTarget {
    fun <T: BrowserEvent> addEventListener(type: String, callback: ((T) -> Unit)?)

    fun <T: BrowserEvent> removeEventListener(type: String, callback: ((T) -> Unit)?)
}

external interface BrowserWindow : BrowserEventTarget {
    val location: BrowserLocation
    val history: BrowserHistory
}

@OptIn(ExperimentalWasmJsInterop::class)
fun refBrowserWindow(): BrowserWindow = js("window")

internal external fun decodeURIComponent(str: String): String

internal external fun encodeURIComponent(str: String): String
