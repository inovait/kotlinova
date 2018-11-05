package si.inova.kotlinova.coroutines

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Collection of Android Coroutine dispatchers that can be overriden for Unit tests.
 *
 * @author Matej Drobnic
 */

/**
 * [kotlinx.coroutines.experimental.android.UI] dispatcher that can be overriden by unit tests.
 */
val UI: CoroutineContext
    get() = dispatcherOverride {
        Dispatchers.Main
    }