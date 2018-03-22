package si.inova.kotlinova.ui.lists.sections

import android.content.Context
import si.inova.kotlinova.ui.lists.LimitingDividerItemDecoration

/**
 * Decorator that displays divider items
 *
 * @author Matej Drobnic
 */
class SectionDividerItemDecoration(
    context: Context,
    orientation: Int,
    private val sectionAdapter: SectionRecyclerAdapter
) :
        LimitingDividerItemDecoration(context, orientation) {

    var sectionsToDisplay: List<Int> = emptyList()

    override fun shouldDisplayDivider(prevousItemIndex: Int): Boolean {
        val section = sectionAdapter.getSectionIndexAtPosition(prevousItemIndex)

        return sectionsToDisplay.contains(section)
    }
}