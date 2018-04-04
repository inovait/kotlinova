package si.inova.kotlinova.ui.lists

import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.view.View
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import si.inova.kotlinova.ui.lists.sections.ListSection
import si.inova.kotlinova.ui.state.StateSaverManager
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * @author Matej Drobnic
 */
class RecyclerStateSaverTest {
    private lateinit var recyclerView: RecyclerView
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var stateSaverManager: StateSaverManager
    private lateinit var recyclerStateSaver: RecyclerStateSaver

    @Before
    fun setUp() {
        stateSaverManager = mock()
        recyclerView = mock()
        layoutManager = mock()

        whenever(recyclerView.layoutManager).thenAnswer { layoutManager }

        val stateSavingComponent: StateSavingComponent = mock()
        whenever(stateSavingComponent.stateSaverManager).thenReturn(stateSaverManager)

        recyclerStateSaver = RecyclerStateSaver(stateSavingComponent, recyclerView, SAVER_KEY)
    }

    @Test
    fun saving() {
        val testParcelable = StringParcelable("Test")
        whenever(layoutManager!!.onSaveInstanceState()).thenReturn(testParcelable)

        assertEquals(testParcelable, recyclerStateSaver.saveState())
    }

    @Test
    fun noLayoutManager() {
        layoutManager = null
        assertNull(recyclerStateSaver.saveState())
    }

    @Test
    fun restoringManual() {
        val testParcelable = StringParcelable("Test")
        whenever(stateSaverManager.getLastLoadedValue<Parcelable>(SAVER_KEY))
                .thenReturn(testParcelable)

        recyclerStateSaver.notifyRecyclerViewLoaded()

        verify(layoutManager!!).onRestoreInstanceState(testParcelable)
    }

    @Test
    fun restoringTwice() {
        val testParcelable = StringParcelable("Test")
        whenever(stateSaverManager.getLastLoadedValue<Parcelable>(SAVER_KEY))
                .thenReturn(testParcelable)

        recyclerStateSaver.notifyRecyclerViewLoaded()
        recyclerStateSaver.notifyRecyclerViewLoaded()

        inOrder(layoutManager!!) {
            verify(layoutManager!!).onRestoreInstanceState(testParcelable)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun restoringListAdapter() {
        val testParcelable = StringParcelable("Test")
        whenever(stateSaverManager.getLastLoadedValue<Parcelable>(SAVER_KEY))
                .thenReturn(testParcelable)

        val listAdapter: ListAdapter<Any, DummyViewHolder> = mock()

        recyclerStateSaver.attach(listAdapter)
        assertNotNull(listAdapter.listUpdateListener)

        listAdapter.listUpdateListener!!.invoke()

        verify(layoutManager!!).onRestoreInstanceState(testParcelable)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun restoringListSection() {
        val testParcelable = StringParcelable("Test")
        whenever(stateSaverManager.getLastLoadedValue<Parcelable>(SAVER_KEY))
                .thenReturn(testParcelable)

        val listSection: ListSection<Any, DummyViewHolder> = mock()

        lateinit var updateListener: () -> Unit
        doAnswer {
            updateListener = it.arguments[0] as () -> Unit
        }.whenever(listSection).listUpdateListener = any()
        recyclerStateSaver.attach(listSection)
        verify(listSection).listUpdateListener = any()

        updateListener.invoke()

        verify(layoutManager!!).onRestoreInstanceState(testParcelable)
    }
}

private const val SAVER_KEY = "Recycler"

class StringParcelable(val text: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StringParcelable> {
        override fun createFromParcel(parcel: Parcel): StringParcelable {
            return StringParcelable(parcel)
        }

        override fun newArray(size: Int): Array<StringParcelable?> {
            return arrayOfNulls(size)
        }
    }
}

class DummyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)