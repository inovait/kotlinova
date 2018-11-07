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

        recyclerSection.onAttachedToAdapter(SectionPassListUpdateCallback(sections.size - 1))
    }

    /**
     * Number of currently attached sections
     */
    val sectionCount: Int
        get() = sections.size

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

    /**
     * Get position inside section from global adapter position
     *
     * @param position global position of adapter
     *
     * @return pair of section at this position and position inside section
     */
    protected fun getInnerPosition(position: Int):
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

    fun getSectionStart(section: RecyclerSection<*>): Int {
        val sectionIndex = sections.indexOf(section)
        if (sectionIndex < 0) {
            throw IllegalArgumentException("Unknown section: $section")
        }

        return getSectionStart(sectionIndex)
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

            if (position + count != section.itemCount) {
                notifyItemRangeInserted(position + sectionStart, count)
                return
            }

            val nextSection = sections.elementAtOrNull(sectionIndex + 1)

            val numPlaceholderItems = if (nextSection?.sectionContainsPlaceholderItems == true) {
                nextSection.itemCount
            } else {
                0
            }

            val numMaxToBlend = count.coerceAtMost(numPlaceholderItems)

            var nextInsertedIndex = 0
            var numUpdated = 0
            while (nextInsertedIndex < numMaxToBlend) {
                val itemType = section.getItemViewType(position + nextInsertedIndex)
                val followingSameType = (nextInsertedIndex until numMaxToBlend).takeWhile {
                    section.getItemViewType(position + it) == itemType
                }.size

                if (section.canBlendIntoPlaceholder(itemType)) {
                    notifyItemRangeChanged(nextInsertedIndex + sectionStart, followingSameType)
                    numUpdated += followingSameType
                } else {
                    notifyItemRangeInserted(nextInsertedIndex + sectionStart, followingSameType)
                }

                nextInsertedIndex += followingSameType
            }

            if (nextInsertedIndex < count || numUpdated > 0) {
                notifyItemRangeInserted(
                    nextInsertedIndex + position + sectionStart,
                    count - nextInsertedIndex + numUpdated
                )
            }
        }

        override fun onRemoved(position: Int, count: Int) {
            val sectionStart = getSectionStart(sectionIndex)
            notifyItemRangeRemoved(position + sectionStart, count)
        }
    }
}

private const val MAX_SECTION_VIEW_TYPE_COUNT = 100_000