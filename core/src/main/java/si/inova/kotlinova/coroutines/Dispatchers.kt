package si.inova.kotlinova.coroutines

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Collection of Coroutine dispatchers that can be overriden for Unit tests.
 *
 * @author Matej Drobnic
 */

/**
 * [kotlinx.coroutines.experimental.CommonPool] dispatcher that can be overriden by unit tests.
 */
val CommonPool
    get() = dispatcherOverride {
        Dispatchers.Default
    }

var dispatcherOverride: (() -> CoroutineContext) -> CoroutineContext = { it() }