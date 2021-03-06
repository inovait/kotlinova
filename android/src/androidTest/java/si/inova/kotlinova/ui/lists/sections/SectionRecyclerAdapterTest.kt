/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.ui.lists.sections

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.only
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy

/**
 * @author Matej Drobnic
 */
class SectionRecyclerAdapterTest {
    @Mock
    private lateinit var adapterDataObserver: RecyclerView.AdapterDataObserver

    @Spy
    private var sectionA = TestSection()

    @Spy
    private var sectionB = TestSection()

    @Spy
    private var sectionC = TestSectionWithDifferentItemTypes()

    private lateinit var sectionedRecyclerAdapter: SectionRecyclerAdapter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        sectionedRecyclerAdapter = SectionRecyclerAdapter()
        sectionedRecyclerAdapter.registerAdapterDataObserver(adapterDataObserver)

        sectionedRecyclerAdapter.attachSection(sectionA)
        sectionedRecyclerAdapter.attachSection(sectionB)
        sectionedRecyclerAdapter.attachSection(sectionC)
    }

    @Test
    fun count() {
        assertEquals(0, sectionedRecyclerAdapter.itemCount)

        sectionA.updateSize(5)
        assertEquals(5, sectionedRecyclerAdapter.itemCount)

        sectionB.updateSize(3)
        assertEquals(5 + 3, sectionedRecyclerAdapter.itemCount)

        sectionC.updateSize(8)
        assertEquals(5 + 3 + 8, sectionedRecyclerAdapter.itemCount)
    }

    @Test
    fun getItemViewTypePositions() {
        sectionA.updateSize(5)
        sectionB.updateSize(3)
        sectionC.updateSize(8)

        inOrder(sectionA, sectionB, sectionC) {
            sectionedRecyclerAdapter.getItemViewType(0)
            verify(sectionA).getItemViewType(0)

            sectionedRecyclerAdapter.getItemViewType(2)
            verify(sectionA).getItemViewType(2)

            sectionedRecyclerAdapter.getItemViewType(4)
            verify(sectionA).getItemViewType(4)

            sectionedRecyclerAdapter.getItemViewType(5)
            verify(sectionB).getItemViewType(0)

            sectionedRecyclerAdapter.getItemViewType(7)
            verify(sectionB).getItemViewType(2)

            sectionedRecyclerAdapter.getItemViewType(9)
            verify(sectionC).getItemViewType(1)

            sectionedRecyclerAdapter.getItemViewType(15)
            verify(sectionC).getItemViewType(7)
        }
    }

    @Test
    fun getItemViewTypeUniqueNumbers() {
        sectionA.updateSize(5)
        sectionB.updateSize(3)
        sectionC.updateSize(8)

        val sectionAType = sectionedRecyclerAdapter.getItemViewType(0)
        val sectionBType = sectionedRecyclerAdapter.getItemViewType(6)
        val sectionCType0 = sectionedRecyclerAdapter.getItemViewType(8)
        val sectionCType1 = sectionedRecyclerAdapter.getItemViewType(9)

        assertNotEquals(sectionAType, sectionBType)
        assertNotEquals(sectionBType, sectionCType0)
        assertNotEquals(sectionCType0, sectionCType1)
    }

    @Test
    fun createViewHolder() {
        sectionA.updateSize(5)
        sectionB.updateSize(3)
        sectionC.updateSize(8)

        val sectionAType = sectionedRecyclerAdapter.getItemViewType(0)
        val sectionBType = sectionedRecyclerAdapter.getItemViewType(6)
        val sectionCType0 = sectionedRecyclerAdapter.getItemViewType(8)
        val sectionCType1 = sectionedRecyclerAdapter.getItemViewType(9)

        val parent = mock<ViewGroup>()

        inOrder(sectionA, sectionB, sectionC) {
            sectionedRecyclerAdapter.onCreateViewHolder(parent, sectionAType)
            verify(sectionA).onCreateViewHolder(parent, 0)

            sectionedRecyclerAdapter.onCreateViewHolder(parent, sectionBType)
            verify(sectionB).onCreateViewHolder(parent, 0)

            sectionedRecyclerAdapter.onCreateViewHolder(parent, sectionCType0)
            verify(sectionC).onCreateViewHolder(parent, 0)

            sectionedRecyclerAdapter.onCreateViewHolder(parent, sectionCType1)
            verify(sectionC).onCreateViewHolder(parent, 1)
        }
    }

    @Test
    fun bindViewHolder() {
        sectionA.updateSize(5)
        sectionB.updateSize(3)
        sectionC.updateSize(8)

        val parent = mock<ViewGroup>()

        val holderA = sectionA.onCreateViewHolder(parent, 0)
        val holderB = sectionB.onCreateViewHolder(parent, 0)
        val holderC0 = sectionC.onCreateViewHolder(parent, 0)
        val holderC7 = sectionC.onCreateViewHolder(parent, 7)

        inOrder(sectionA, sectionB, sectionC) {
            sectionedRecyclerAdapter.onBindViewHolder(holderA, 0)
            verify(sectionA).onBindViewHolder(holderA, 0)

            sectionedRecyclerAdapter.onBindViewHolder(holderA, 2)
            verify(sectionA).onBindViewHolder(holderA, 2)

            sectionedRecyclerAdapter.onBindViewHolder(holderA, 4)
            verify(sectionA).onBindViewHolder(holderA, 4)

            sectionedRecyclerAdapter.onBindViewHolder(holderB, 5)
            verify(sectionB).onBindViewHolder(holderB, 0)

            sectionedRecyclerAdapter.onBindViewHolder(holderB, 7)
            verify(sectionB).onBindViewHolder(holderB, 2)

            sectionedRecyclerAdapter.onBindViewHolder(holderC0, 8)
            verify(sectionC).onBindViewHolder(holderC0, 0)

            sectionedRecyclerAdapter.onBindViewHolder(holderC7, 15)
            verify(sectionC).onBindViewHolder(holderC7, 7)
        }
    }

    @Test
    fun insertUpdate() {
        inOrder(adapterDataObserver) {
            sectionA.updateSize(5)
            verify(adapterDataObserver).onItemRangeInserted(0, 5)

            sectionB.updateSize(3)
            verify(adapterDataObserver).onItemRangeInserted(5, 3)

            sectionC.updateSize(8)
            verify(adapterDataObserver).onItemRangeInserted(5 + 3, 8)

            sectionA.updateSize(7)
            verify(adapterDataObserver).onItemRangeInserted(5, 2)

            sectionC.updateSize(9, sendCallback = false)
            sectionC.updateCallback!!.onInserted(2, 1)
            verify(adapterDataObserver).onItemRangeInserted(7 + 3 + 2, 1)
        }
    }

    @Test
    fun removeUpdate() {
        sectionA.updateSize(5)
        sectionB.updateSize(3)
        sectionC.updateSize(8)

        inOrder(adapterDataObserver) {
            sectionC.updateSize(7)
            verify(adapterDataObserver).onItemRangeRemoved(5 + 3 + 7, 1)

            sectionB.updateSize(2)
            verify(adapterDataObserver).onItemRangeRemoved(5 + 2, 1)

            sectionA.updateSize(4)
            verify(adapterDataObserver).onItemRangeRemoved(4, 1)

            sectionC.updateSize(2)
            verify(adapterDataObserver).onItemRangeRemoved(4 + 2 + 2, 5)

            sectionB.updateSize(1)
            verify(adapterDataObserver).onItemRangeRemoved(4 + 1, 1)

            sectionC.updateSize(1)
            verify(adapterDataObserver).onItemRangeRemoved(4 + 1 + 1, 1)

            sectionA.updateSize(2)
            verify(adapterDataObserver).onItemRangeRemoved(2, 2)
        }
    }

    @Test
    fun moveUpdate() {
        sectionA.updateSize(5)
        sectionB.updateSize(3)
        sectionC.updateSize(8)

        inOrder(adapterDataObserver) {
            sectionA.updateCallback!!.onMoved(0, 2)
            verify(adapterDataObserver).onItemRangeMoved(0, 2, 1)
            sectionA.updateCallback!!.onMoved(1, 3)
            verify(adapterDataObserver).onItemRangeMoved(1, 3, 1)

            sectionB.updateCallback!!.onMoved(0, 3)
            verify(adapterDataObserver).onItemRangeMoved(5 + 0, 5 + 3, 1)
            sectionB.updateCallback!!.onMoved(2, 1)
            verify(adapterDataObserver).onItemRangeMoved(5 + 2, 5 + 1, 1)

            sectionC.updateCallback!!.onMoved(6, 2)
            verify(adapterDataObserver).onItemRangeMoved(5 + 3 + 6, 5 + 3 + 2, 1)
            sectionC.updateCallback!!.onMoved(2, 6)
            verify(adapterDataObserver).onItemRangeMoved(5 + 3 + 2, 5 + 3 + 6, 1)
        }
    }

    @Test
    fun changeUpdate() {
        sectionA.updateSize(5)
        sectionB.updateSize(3)
        sectionC.updateSize(8)

        inOrder(adapterDataObserver) {
            sectionA.updateCallback!!.onChanged(0, 2, null)
            verify(adapterDataObserver).onItemRangeChanged(0, 2, null)
            sectionA.updateCallback!!.onChanged(2, 1, null)
            verify(adapterDataObserver).onItemRangeChanged(2, 1, null)

            sectionB.updateCallback!!.onChanged(2, 1, null)
            verify(adapterDataObserver).onItemRangeChanged(5 + 2, 1, null)
            sectionB.updateCallback!!.onChanged(0, 2, null)
            verify(adapterDataObserver).onItemRangeChanged(5 + 0, 2, null)

            sectionC.updateCallback!!.onChanged(0, 8, null)
            verify(adapterDataObserver).onItemRangeChanged(5 + 3 + 0, 8, null)
            sectionC.updateCallback!!.onChanged(5, 2, null)
            verify(adapterDataObserver).onItemRangeChanged(5 + 3 + 5, 2, null)
        }
    }

    @Test
    fun realItemCount() {
        sectionA.updateSize(2)
        sectionB.updateSize(4)
        sectionC.updateSize(6)

        assertEquals(2 + 4 + 6, sectionedRecyclerAdapter.realItemCount)
        assertEquals(2 + 4 + 6, sectionedRecyclerAdapter.itemCount)

        val placeholderSection = PlaceholderTestSection()
        sectionedRecyclerAdapter.attachSection(placeholderSection)
        placeholderSection.updateSize(20)

        assertEquals(2 + 4 + 6, sectionedRecyclerAdapter.realItemCount)
        assertEquals(2 + 4 + 6 + 20, sectionedRecyclerAdapter.itemCount)
    }

    @Test
    fun placeholderBlending() {
        val blendingSection = BlendingSection()
        val placeholderSection = PlaceholderTestSection().apply { data = MutableList(100) { it } }
        sectionedRecyclerAdapter.attachSection(blendingSection)
        sectionedRecyclerAdapter.attachSection(placeholderSection)

        assertEquals(100, sectionedRecyclerAdapter.itemCount)
        blendingSection.updateSize(10)

        verify(adapterDataObserver).onItemRangeChanged(0, 10, null)
        verify(adapterDataObserver).onItemRangeInserted(10, 10)
        verifyNoMoreInteractions(adapterDataObserver)
        assertEquals(10 + 100, sectionedRecyclerAdapter.itemCount)
    }

    @Test
    fun placeholderBlendingMoreThanHundred() {
        val blendingSection = BlendingSection()
        val placeholderSection = PlaceholderTestSection().apply { data = MutableList(100) { it } }
        sectionedRecyclerAdapter.attachSection(blendingSection)
        sectionedRecyclerAdapter.attachSection(placeholderSection)

        assertEquals(100, sectionedRecyclerAdapter.itemCount)
        blendingSection.updateSize(300)

        verify(adapterDataObserver).onItemRangeChanged(0, 100, null)
        verify(adapterDataObserver).onItemRangeInserted(100, 300)
        verifyNoMoreInteractions(adapterDataObserver)
        assertEquals(300 + 100, sectionedRecyclerAdapter.itemCount)
    }

    @Test
    fun placeholderBlendingWithEmptySectionInBetween() {
        val blendingSection = BlendingSection()
        val emptySection = TestSection()
        val placeholderSection = PlaceholderTestSection().apply { data = MutableList(100) { it } }
        sectionedRecyclerAdapter.attachSection(blendingSection)
        sectionedRecyclerAdapter.attachSection(emptySection)
        sectionedRecyclerAdapter.attachSection(placeholderSection)

        assertEquals(100, sectionedRecyclerAdapter.itemCount)
        blendingSection.updateSize(10)

        verify(adapterDataObserver).onItemRangeChanged(0, 10, null)
        verify(adapterDataObserver).onItemRangeInserted(10, 10)
        verifyNoMoreInteractions(adapterDataObserver)
        assertEquals(10 + 100, sectionedRecyclerAdapter.itemCount)
    }

    @Test
    fun doNotBlendPlaceholdersWithNotEmptySectionInBetween() {
        val blendingSection = BlendingSection()
        val middleSection = TestSection().apply { updateSize(1) }
        val placeholderSection = PlaceholderTestSection().apply { data = MutableList(100) { it } }

        reset(adapterDataObserver)

        sectionedRecyclerAdapter.attachSection(blendingSection)
        sectionedRecyclerAdapter.attachSection(middleSection)
        sectionedRecyclerAdapter.attachSection(placeholderSection)

        assertEquals(0 + 1 + 100, sectionedRecyclerAdapter.itemCount)
        blendingSection.updateSize(10)

        verify(adapterDataObserver).onItemRangeInserted(0, 10)
        verifyNoMoreInteractions(adapterDataObserver)
        assertEquals(10 + 1 + 100, sectionedRecyclerAdapter.itemCount)
    }

    @Test
    fun doNotBlendPlaceholdersWhenNotOnEnd() {
        val blendingSection = BlendingSection()
        val placeholderSection = PlaceholderTestSection().apply { data = MutableList(100) { it } }
        sectionedRecyclerAdapter.attachSection(blendingSection)
        blendingSection.updateSize(10)
        verify(adapterDataObserver).onItemRangeInserted(0, 10)

        sectionedRecyclerAdapter.attachSection(placeholderSection)

        blendingSection.data.add(1, 10)
        blendingSection.updateCallback!!.onInserted(1, 1)
        verify(adapterDataObserver).onItemRangeInserted(1, 1)
        verifyNoMoreInteractions(adapterDataObserver)

        assertEquals(11 + 100, sectionedRecyclerAdapter.itemCount)
    }

    @Test
    fun doNotBlendWhenPlaceholderSectionIsEmpty() {
        val blendingSection = BlendingSection()
        val placeholderSection = PlaceholderTestSection().apply { data = MutableList(0) { it } }

        sectionedRecyclerAdapter.attachSection(blendingSection)
        sectionedRecyclerAdapter.attachSection(placeholderSection)

        assertEquals(0, sectionedRecyclerAdapter.itemCount)
        blendingSection.updateSize(10)

        verify(adapterDataObserver, only()).onItemRangeInserted(0, 10)
        assertEquals(10, sectionedRecyclerAdapter.itemCount)
    }

    @Test
    fun sectionCount() {
        assertEquals(3, sectionedRecyclerAdapter.sectionCount)

        sectionedRecyclerAdapter.attachSection(TestSection())
        sectionedRecyclerAdapter.attachSection(TestSection())

        assertEquals(5, sectionedRecyclerAdapter.sectionCount)
    }

    @Test
    fun partialPlaceholderBlending() {
        val blendingSection = PartialBlendingSection()
        val placeholderSection = PlaceholderTestSection().apply { data = MutableList(15) { it } }
        sectionedRecyclerAdapter.attachSection(blendingSection)
        sectionedRecyclerAdapter.attachSection(placeholderSection)

        assertEquals(15, sectionedRecyclerAdapter.itemCount)

        blendingSection.updateSize(20)

        verify(adapterDataObserver).onItemRangeInserted(0, 1)
        verify(adapterDataObserver).onItemRangeChanged(1, 2, null)
        verify(adapterDataObserver).onItemRangeInserted(3, 4)
        verify(adapterDataObserver).onItemRangeChanged(7, 8, null)
        verify(adapterDataObserver).onItemRangeInserted(15, 5 + 10)
        verifyNoMoreInteractions(adapterDataObserver)

        assertEquals(20 + 15, sectionedRecyclerAdapter.itemCount)
    }

    private class TestSection : RecyclerSection<TestSectionViewHolder>() {
        var data: MutableList<Int> = ArrayList()

        public override var updateCallback: ListUpdateCallback?
            get() = super.updateCallback
            set(value) {
                super.updateCallback = value
            }

        fun updateSize(newSize: Int, sendCallback: Boolean = true) {
            val oldSize = data.size
            data = MutableList(newSize) { it }

            if (sendCallback) {
                if (oldSize < newSize) {
                    updateCallback?.onInserted(oldSize, newSize - oldSize)
                } else {
                    updateCallback?.onRemoved(newSize, oldSize - newSize)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestSectionViewHolder {
            return TestSectionViewHolder(mock())
        }

        override fun onBindViewHolder(holder: TestSectionViewHolder, position: Int) {
            holder.lastBoundPosition = position
        }

        override val itemCount: Int
            get() = data.size
    }

    private class PartialBlendingSection : TestSection() {
        override fun canBlendIntoPlaceholder(itemType: Int): Boolean = itemType == 0

        override fun getItemViewType(position: Int): Int {
            val allowBlendType = position in 1..2 || position >= 7

            return if (allowBlendType) 0 else 1
        }
    }

    private class BlendingSection : TestSection() {
        override fun canBlendIntoPlaceholder(itemType: Int): Boolean = true
    }

    private class PlaceholderTestSection : TestSection() {
        override val sectionContainsPlaceholderItems: Boolean
            get() = true
    }

    private class TestSectionWithDifferentItemTypes : TestSection() {
        override fun getItemViewType(position: Int): Int {
            return position
        }
    }

    private class TestSectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var lastBoundPosition: Int = 0
    }
}