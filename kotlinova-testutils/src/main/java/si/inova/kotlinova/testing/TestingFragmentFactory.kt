package si.inova.kotlinova.testing

import android.support.v4.app.Fragment
import org.junit.Assert

/**
 * Fragment factory that creates dummy fragments and binds them to the types of the original fragments
 * So they can be identified
 *
 * @author Matej Drobnic
 */
class TestingFragmentFactory : (() -> Fragment) -> Fragment {
    val fragmentMap = HashMap<Class<out Fragment>, Fragment>()

    override fun invoke(createOriginal: () -> Fragment): Fragment {
        val originalFragment = createOriginal()
        val newFragment = Fragment()

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
}