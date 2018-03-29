package si.inova.kotlinova.testing

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.widget.LinearLayout

/**
 * Dummy activity class for testing fragments
 * @author Matej Drobnic
 */
class DummyFragmentActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LinearLayout(this)
        view.id = 1

        setContentView(view)
    }
}