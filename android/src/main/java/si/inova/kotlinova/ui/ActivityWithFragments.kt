package si.inova.kotlinova.ui

import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import si.inova.kotlinova.ui.components.StateSaverActivity
import javax.inject.Inject

/**
 * Activity that contains fragments.
 *
 * Implementations include dependency injection and fragment mocking.
 *
 * @author Matej Drobnic
 */
abstract class ActivityWithFragments : StateSaverActivity(), HasSupportFragmentInjector {
    @Inject
    @JvmField
    var fragmentInjector: DispatchingAndroidInjector<Fragment>? = null

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector
        ?: AndroidInjector { }

    var createFragment: (() -> Fragment) -> Fragment = { it() }
}