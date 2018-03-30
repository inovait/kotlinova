package si.inova.kotlinova.ui.lists

import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import si.inova.kotlinova.ui.lists.sections.ListSection
import si.inova.kotlinova.ui.state.StateSaver
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * State saver that saves [RecyclerView]'s state and then loads it at any point in time
 * (whenever data is reloaded).
 *
 * You must manually call [notifyRecyclerViewLoaded]&#40;) or if you are using [ListAdapter] or [ListSection], you can
 * instead call [attach]&#40;) to automatically load data when ListAdapter receives new data.
 *
 * @author Matej Drobnic
 */
class RecyclerStateSaver(
    component: StateSavingComponent,
    private val recycler: RecyclerView,
    key: String
) : StateSaver<Parcelable>() {

    private var alreadyRestored = false

    init {
        register(component.stateSaverManager, key)
    }

    override fun saveState(): Parcelable? {
        return recycler.layoutManager?.onSaveInstanceState()
    }

    fun notifyRecyclerViewLoaded() {
        if (alreadyRestored) {
            return
        }

        val lastSavedState = lastSavedState ?: return
        recycler.layoutManager?.onRestoreInstanceState(lastSavedState)
        alreadyRestored = true
    }

    fun attach(listAdapter: ListAdapter<*, *>) {
        listAdapter.listUpdateListener = this::notifyRecyclerViewLoaded
    }

    fun attach(listSection: ListSection<*, *>) {
        listSection.listUpdateListener = this::notifyRecyclerViewLoaded
    }
}