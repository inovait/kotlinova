package si.inova.kotlinova.ui.components

import android.app.Activity
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import si.inova.kotlinova.ui.state.StateSaverManager
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * Base fragment that provides automatic state saving facilities out of the box
 *
 * @author Matej Drobnic
 */
open class StateSaverFragment : Fragment(), StateSavingComponent {
    override val stateSaverManager = StateSaverManager()

    /**
     * Whether this fragment has just been created for the first time.
     *
     * *true* means fragment has been created for the first time OR when fragment has been
     * recreated after process has been completely destroyed (for example due to low memory).
     *
     * *false* means fragment has been recreated from the previous state
     * (for example when being recreated from configuration change or from backstack)
     */
    var createdForTheFirstTime = true
        private set

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            stateSaverManager.restoreInstance(savedInstanceState)
        }

        super.onCreate(savedInstanceState)

        createdForTheFirstTime = createdForTheFirstTime &&
            (savedInstanceState == null ||
                (context as? Activity)?.lastNonConfigurationInstance == null)
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        stateSaverManager.saveInstance(outState)
        super.onSaveInstanceState(outState)
    }

    @CallSuper
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        createdForTheFirstTime = false
    }
}