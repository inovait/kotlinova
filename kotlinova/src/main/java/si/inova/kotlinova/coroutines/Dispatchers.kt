package si.inova.kotlinova.coroutines

import kotlinx.coroutines.experimental.CoroutineDispatcher

/**
 * Collection of Coroutine dispatchers that can be overriden for Unit tests.
 *
 * @author Matej Drobnic
 */

/**
 * [kotlinx.coroutines.experimental.android.UI] dispatcher that can be overriden by unit tests.
 */
val UI: CoroutineDispatcher
    get() = dispatcherOverride { kotlinx.coroutines.experimental.android.UI }

/**
 * [kotlinx.coroutines.experimental.CommonPool] dispatcher that can be overriden by unit tests.
 */
val CommonPool
    get() = dispatcherOverride { kotlinx.coroutines.experimental.CommonPool }

var dispatcherOverride: (() -> CoroutineDispatcher) -> CoroutineDispatcher = { it() }