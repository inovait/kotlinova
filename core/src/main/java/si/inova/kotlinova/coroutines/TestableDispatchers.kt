package si.inova.kotlinova.coroutines

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Collection of Coroutine dispatchers that can be overriden for Unit tests.
 **/
object TestableDispatchers {
    /**
     * [Dispatchers.Default] dispatcher that can be overriden by unit tests.
     */
    @JvmStatic
    val Default
        get() = dispatcherOverride {
            Dispatchers.Default
        }

    /**
     * [Dispatchers.Main] dispatcher that can be overriden by unit tests.
     */
    @JvmStatic
    val Main
        get() = dispatcherOverride {
            Dispatchers.Main
        }

    /**
     * [Dispatchers.IO] dispatcher that can be overriden by unit tests.
     */
    @JvmStatic
    val IO
        get() = dispatcherOverride {
            Dispatchers.IO
        }

    @JvmStatic
    var dispatcherOverride: (() -> CoroutineContext) -> CoroutineContext = { it() }
}
