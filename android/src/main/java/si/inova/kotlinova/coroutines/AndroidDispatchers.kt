package si.inova.kotlinova.coroutines

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Dispatchers

/**
 * Collection of Android Coroutine dispatchers that can be overriden for Unit tests.
 *
 * @author Matej Drobnic
 */

/**
 * [kotlinx.coroutines.experimental.android.UI] dispatcher that can be overriden by unit tests.
 */
val UI: CoroutineDispatcher
    get() = dispatcherOverride {
        Dispatchers.Main
    }