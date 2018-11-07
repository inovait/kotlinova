package si.inova.kotlinova.ui.state

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View

/**
 * Dummy invisible View that serves as [StateSavingComponent] for view state.
 * This is useful for Fragments where view state is saved separately from fragment state.
 *
 * @author Matej Drobnic
 */
class StateSavingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), StateSavingComponent {
    override val stateSaverManager = StateSaverManager()

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        stateSaverManager.saveInstance(bundle)

        return State(superState, bundle)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        state as State

        stateSaverManager.restoreInstance(state.data)
        super.onRestoreInstanceState(state.superState)
    }

    class State : BaseSavedState {
        val data: Bundle

        constructor(superState: Parcelable?, data: Bundle) : super(superState) {
            this.data = data
        }

        constructor(source: Parcel) : super(source) {
            data = source.readBundle(javaClass.classLoader) ?: error("System returned null bundle")
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeBundle(data)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<State> {
            override fun createFromParcel(parcel: Parcel): State {
                return State(parcel)
            }

            override fun newArray(size: Int): Array<State?> {
                return arrayOfNulls(size)
            }
        }
    }
}