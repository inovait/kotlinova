package si.inova.kotlinova.ui.lists

import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.widget.AdapterView
import si.inova.kotlinova.ui.state.StateSaver
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * State saver that saves any [AdapterView]'s state and then loads it at any point in time
 * (whenever data is reloaded).
 *
 * You must manually call [notifyDataLoaded]&#40;) whenever data gets loaded into linked AdapterView.
 *
 * @author Matej Drobnic
 */
class AdapterViewStateSaver(
    component: StateSavingComponent,
    private val adapterView: AdapterView<*>,
    key: String
) : StateSaver<Parcelable>() {

    private var alreadyRestored = false

    init {
        register(component.stateSaverManager, key)
    }

    override fun saveState(): Parcelable? {
        val viewId = adapterView.id
        if (viewId == View.NO_ID) {
            throw IllegalStateException("To save state, AdapterView must have an ID!")
        }

        val container = SparseArray<Parcelable>()
        adapterView.saveHierarchyState(container)
        return container.get(viewId)
    }

    fun notifyDataLoaded() {
        val lastSavedState = lastSavedState ?: return
        if (alreadyRestored) {
            return
        }

        val viewId = adapterView.id
        if (viewId == View.NO_ID) {
            throw IllegalStateException("To save state, AdapterView must have an ID!")
        }

        val container = SparseArray<Parcelable>()
        container.put(viewId, lastSavedState)
        adapterView.restoreHierarchyState(container)
        alreadyRestored = true
    }
}