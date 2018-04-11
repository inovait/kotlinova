package si.inova.kotlinova.ui

/**
 * Interface that allows [Fragment] to be resettable (for example list/scroll is reset by scrolling to top).
 * This is primarily used with [BottomNavigationMenu] when user re-selects page that user is already on.
 *
 * @author janko
 */
interface ResettableFragment {
    fun resetFragment()
}