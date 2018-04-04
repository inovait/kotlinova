package si.inova.kotlinova.ui.lists.sections

import android.content.Context
import si.inova.kotlinova.R

/**
 * Section that displays indeterminate progress bar. Hidden by default.
 *
 * @author Matej Drobnic
 */
class LoadingSection(context: Context) : SingleViewSection(context, R.layout.item_loading) {
    init {
        displayed = false
    }
}