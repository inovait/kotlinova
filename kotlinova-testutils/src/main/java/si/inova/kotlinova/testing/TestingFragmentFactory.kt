package si.inova.kotlinova.testing

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.setContext
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