package si.inova.kotlinova.ui.lists.sections

import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.only
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.robolectric.RobolectricTestRunner
import si.inova.kotlinova.R

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
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

            sectionC.updateSize(9)
            verify(adapterDataObserver).onItemRangeInserted(7 + 3 + 8, 1)
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
        val placeholderSection = PlaceholderSection(R.layout.single_container)
        sectionedRecyclerAdapter.attachSection(blendingSection)
        sectionedRecyclerAdapter.attachSection(placeholderSection)

        assertEquals(Short.MAX_VALUE.toInt(), sectionedRecyclerAdapter.itemCount)
        blendingSection.updateSize(10)

        verify(adapterDataObserver, only()).onItemRangeChanged(0, 10, null)
        assertEquals(Short.MAX_VALUE.toInt(), sectionedRecyclerAdapter.itemCount)
    }

    @Test
    fun doNotBlendPlaceholdersWhenNotOnEnd() {
        val blendingSection = BlendingSection()
        val placeholderSection = PlaceholderSection(R.layout.single_container)
        sectionedRecyclerAdapter.attachSection(blendingSection)
        sectionedRecyclerAdapter.attachSection(placeholderSection)

        inOrder(adapterDataObserver) {
            blendingSection.updateSize(10)
            verify(adapterDataObserver).onItemRangeChanged(0, 10, null)

            blendingSection.data.add(1, 10)
            blendingSection.updateCallback!!.onInserted(1, 1)

            verify(adapterDataObserver).onItemRangeInserted(1, 1)

            assertEquals(Short.MAX_VALUE.toInt() + 1, sectionedRecyclerAdapter.itemCount)
        }
    }

    private class TestSection : RecyclerSection<TestSectionViewHolder>() {
        var data: MutableList<Int> = ArrayList()

        public override var updateCallback: ListUpdateCallback?
            get() = super.updateCallback!!
            set(value) {
                super.updateCallback = value
            }

        fun updateSize(newSize: Int) {
            val oldSize = data.size
            data = MutableList(newSize) { it }

            if (oldSize < newSize) {
                updateCallback!!.onInserted(oldSize, newSize - oldSize)
            } else {
                updateCallback!!.onRemoved(newSize, oldSize - newSize)
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

    private class BlendingSection : TestSection() {
        override val blendsIntoPlaceholders: Boolean
            get() = true
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

    private class TestSectionViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var lastBoundPosition: Int = 0
    }
}