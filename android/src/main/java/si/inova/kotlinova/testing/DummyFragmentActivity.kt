package si.inova.kotlinova.testing

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import si.inova.kotlinova.android.R

/**
 * Dummy activity class for testing fragments
 */
open class DummyFragmentActivity : FragmentActivity() {
    protected var targetFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.single_container)
        attemptSetup()
    }

    protected open fun attemptSetup() {
        val targetFragment = targetFragment

        if (targetFragment == null ||
            supportFragmentManager.findFragmentById(R.id.container) != null
        ) {
            return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, targetFragment)
            .commitNowAllowingStateLoss()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        attemptSetup()

        super.onConfigurationChanged(newConfig)
    }

    open fun swapFragment(fragment: Fragment) {
        check(targetFragment == null) { "Target fragment already set to $targetFragment" }

        targetFragment = fragment
        attemptSetup()
    }
}