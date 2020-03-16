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

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.setContext
import org.junit.Assert
import si.inova.kotlinova.ui.SubtitledComponent
import si.inova.kotlinova.ui.TitledComponent

/**
 * Fragment factory that creates dummy fragments and binds them to the types of the original fragments
 * So they can be identified.
 *
 * @author Matej Drobnic
 */
class TestingFragmentFactory : (() -> Fragment) -> Fragment {
    val fragmentMap = HashMap<Class<out Fragment>, Fragment>()

    override fun invoke(createOriginal: () -> Fragment): Fragment {
        val originalFragment = createOriginal()

        val newFragment = when (originalFragment) {
            is SubtitledComponent -> TestSubTitledFragment().apply {
                this.originalFragment = originalFragment
            }
            is TitledComponent -> TestTitledFragment().apply {
                this.originalFragment = originalFragment
            }
            else -> Fragment()
        }

        fragmentMap[originalFragment::class.java] = newFragment
        return newFragment
    }

    /**
     * Asser that if [fragment] would be created normally, it would be of provided
     * type [T].
     */
    inline fun <reified T : Fragment> assertFragmentIs(fragment: Fragment?) {
        Assert.assertNotNull("Supplied fragment is null", fragment)

        val fragmentOfThatClass = fragmentMap[T::class.java]
        Assert
                .assertNotNull(
                        "Fragment of type ${T::class.java.name} was not created",
                        fragmentOfThatClass
                )
        Assert
                .assertSame(
                        "Supplied Fragment is not of type ${T::class.java.name}",
                        fragmentOfThatClass, fragment
                )
    }

    class TestTitledFragment : Fragment(),
            TitledComponent {
        var originalFragment: TitledComponent? = null

        override val title: String
            get() = originalFragment?.title ?: ""

        override fun onAttach(context: Context) {
            (originalFragment as? Fragment)?.setContext(context)

            super.onAttach(context)
        }
    }

    class TestSubTitledFragment : Fragment(),
            SubtitledComponent {
        var originalFragment: SubtitledComponent? = null

        override val title: String
            get() = originalFragment?.title ?: ""
        override val subtitle: String
            get() = originalFragment?.subtitle ?: ""

        override fun onAttach(context: Context) {
            (originalFragment as? Fragment)?.setContext(context)

            super.onAttach(context)
        }
    }
}