package si.inova.kotlinova.testing

/**
 * Local extensions for Kotlin's function interfaces.
 * Those classes have issues with native Robolectric mocking, creating dummy local extensions fixes that.
 *
 * @author Matej Drobnic
 */
interface LocalFunction0<out R> : Function0<R>

interface LocalFunction1<in P1, out R> : Function1<P1, R>