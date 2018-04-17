package si.inova.kotlinova.ui.lists.sections

import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import si.inova.kotlinova.testing.OpenForTesting

/**
 * Wrapper adapter for RecyclerView that can display different subsequent sections.
 *
 * After initializing adapter, you need to
 * add sections using [attachSection(RecyclerSection)][attachSection] method
 *
 * @author Matej Drobnic
 */
@OpenForTesting
class SectionRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val sections = ArrayList<RecyclerSection<out RecyclerView.ViewHolder>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val sectionIndex = viewType / MAX_SECTION_VIEW_TYPE_COUNT
        val innerViewType = viewType % MAX_SECTION_VIEW_TYPE_COUNT

        return sections[sectionIndex].onCreateViewHolder(parent, innerViewType)
    }

    override fun getItemCount(): Int {
        return sections.sumBy { it.itemCount }
    }

    /**
     * Count of all items in this adapter, excluding placeholder items
     */
    val realItemCount: Int
        get() {
            return sections.sumBy {
                if (it.sectionContainsPlaceholderItems) {
                    0
                } else {
                    it.itemCount
                }
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (section, innerPosition) = getInnerPosition(position)

        @Suppress("UNCHECKED_CAST")
        section as RecyclerSection<RecyclerView.ViewHolder>
        section.onBindViewHolder(holder, innerPosition)
    }

    override fun getItemViewType(position: Int): Int {
        val (section, innerPosition) = getInnerPosition(position)
        val index = sections.indexOf(section)

        val innerViewType = section.getItemViewType(innerPosition)
        return index * MAX_SECTION_VIEW_TYPE_COUNT + innerViewType
    }

    final fun attachSection(recyclerSection: RecyclerSection<out RecyclerView.ViewHolder>) {
        sections.add(recyclerSection)

        recyclerSection.onAttachedToRecycler(SectionPassListUpdateCallback(sections.size - 1))
    }

    fun getSectionIndexAtPosition(position: Int): Int {
        var sectionStart = 0
        for ((index, section) in sections.withIndex()) {
            val sectionEnd = sectionStart + section.itemCount

            if (sectionEnd > position) {
                return index
            }

            sectionStart = sectionEnd
        }

        throw IndexOutOfBoundsException("Position not in any section: $position")
    }

    private fun getInnerPosition(position: Int):
        Pair<RecyclerSection<out RecyclerView.ViewHolder>, Int> {
        var sectionStart = 0
        for (section in sections) {
            val sectionEnd = sectionStart + section.itemCount

            if (sectionEnd > position) {
                val indexWithinSection = position - sectionStart
                return Pair(section, indexWithinSection)
            }

            sectionStart = sectionEnd
        }

        throw IndexOutOfBoundsException("Position not in any section: $position")
    }

    private fun getSectionStart(sectionIndex: Int): Int {
        var sectionStart = 0
        for (i in 0 until sectionIndex) {
            val section = sections[i]

            sectionStart += section.itemCount
        }

        return sectionStart
    }

    inner class SectionPassListUpdateCallback(private val sectionIndex: Int) : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {
            val sectionStart = getSectionStart(sectionIndex)
            notifyItemRangeChanged(position + sectionStart, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            val sectionStart = getSectionStart(sectionIndex)
            notifyItemMoved(fromPosition + sectionStart, toPosition + sectionStart)
        }

        override fun onInserted(position: Int, count: Int) {
            val sectionStart = getSectionStart(sectionIndex)

            val section = sections[sectionIndex]
            val nextSection = sections.elementAtOrNull(sectionIndex + 1)
            if (section.blendsIntoPlaceholders &&
                nextSection is PlaceholderSection &&
                (position + count) == section.itemCount) {
                nextSection.removePlaceholdersFromStart(count)
                notifyItemRangeChanged(position + sectionStart, count)
                return
            }

            notifyItemRangeInserted(position + sectionStart, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            val sectionStart = getSectionStart(sectionIndex)
            notifyItemRangeRemoved(position + sectionStart, count)
        }
    }
}

private const val MAX_SECTION_VIEW_TYPE_COUNT = 100_000