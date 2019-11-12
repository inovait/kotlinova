package si.inova.kotlinova.testing

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import si.inova.kotlinova.ui.ActivityWithFragments

/**
 * @author Matej Drobnic
 */
class FragmentTestActivity : ActivityWithFragments(), HasSupportFragmentInjector {

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this).apply { id = 1 })
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return AndroidInjector { }
    }
}

@Deprecated("Do not use Robolectric. Migrate to regular unit tests or instrumented tests.")
fun createFragmentTestActivity(): ActivityController<FragmentTestActivity> {
    return Robolectric.buildActivity(FragmentTestActivity::class.java)
}

@SuppressLint("ResourceType")
@Deprecated("Do not use Robolectric. Migrate to regular unit tests or instrumented tests.")
fun ActivityController<FragmentTestActivity>.startDaggerFragment(fragment: Fragment) {
    create()
    get()
        .supportFragmentManager
        .beginTransaction()
        .replace(1, fragment, null)
        .commit()

    start().resume()
}

/**
 * Start fragment that has dagger dependencies (without injecting any of them)
 */
fun startDaggerFragment(fragment: Fragment): ActivityController<FragmentTestActivity> {
    return createFragmentTestActivity().also { it.startDaggerFragment(fragment) }
}