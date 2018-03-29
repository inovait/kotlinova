package si.inova.kotlinova.ui.components

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v7.app.AppCompatActivity
import si.inova.kotlinova.ui.state.StateSaverManager
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * Base activity that provides automatic state saving facilities out of the box
 *
 * @author Matej Drobnic
 */
abstract class StateSaverActivity : AppCompatActivity(), StateSavingComponent {
    override val stateSaverManager = StateSaverManager()

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            stateSaverManager.restoreInstance(savedInstanceState)
        }

        super.onCreate(savedInstanceState)
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        stateSaverManager.saveInstance(outState)
        super.onSaveInstanceState(outState)
    }
}