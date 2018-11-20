package si.inova.kotlinova.ui.state

import android.os.Bundle
import androidx.collection.ArrayMap
import si.inova.kotlinova.testing.OpenForTesting
import si.inova.kotlinova.utils.set

/**
 * @author Matej Drobnic
 *
 * Helper class that handles state savings of [StateSaver].
 */

@OpenForTesting
class StateSaverManager {
    private var lastLoadedBundle: Bundle? = null
    private val variablesMap = ArrayMap<String, StateSaver<*>>()

    fun registerStateSaver(key: String, stateSaver: StateSaver<*>) {
        variablesMap[key] = stateSaver

        if (lastLoadedBundle != null) {
            stateSaver.loadState(getLastLoadedValue(key))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getLastLoadedValue(key: String): T? {
        return lastLoadedBundle?.get(key) as T?
    }

    fun restoreInstance(state: Bundle) {
        lastLoadedBundle = Bundle(state)

        for ((key, stateSaver) in variablesMap) {
            stateSaver.loadState(getLastLoadedValue(key))
        }
    }

    fun saveInstance(state: Bundle) {
        for ((key, stateSaver) in variablesMap) {
            val targetValue = stateSaver.saveState()

            if (targetValue == null) {
                state.remove(key)
            } else {
                state[key] = targetValue
            }
        }
    }
}