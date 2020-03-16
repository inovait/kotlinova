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

package si.inova.kotlinova.ui.lists

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
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
        doAnswer {
            val runnable: Runnable = it.getArgument(0)
            runnable.run()
            true
        }.whenever(recyclerView).post(any())

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
        }.whenever(listSection).addUpdateListener(any())

        recyclerStateSaver.attach(listSection)
        verify(listSection).addUpdateListener(any())

        updateListener.invoke()

        verify(layoutManager!!).onRestoreInstanceState(testParcelable)
    }
}

private const val SAVER_KEY = "Recycler"

class DummyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class StringParcelable(val text: String) : Parcelable {
    constructor(parcel: Parcel) :
            this(parcel.readString() ?: error("System returned null string"))

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