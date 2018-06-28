package android.support.v4.app

import android.os.Bundle

/**
 * Methods in the same package than [Fragment] class, allowing accessing its package-private members.
 * @author Matej Drobnic
 */
fun Fragment.nextAnim(): Int {
    return nextAnim
}

fun Fragment.restoreViewState(savedInstanceState: Bundle?) {
    this.restoreViewState(savedInstanceState)
}