package si.inova.kotlinova.ui.lists.sections

import si.inova.kotlinova.android.R

/**
 * Section that displays indeterminate progress bar. Hidden by default.
 *
 * @author Matej Drobnic
 */
class LoadingSection : SingleViewSection(R.layout.item_loading) {
    init {
        displayed = false
    }
}