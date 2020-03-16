/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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