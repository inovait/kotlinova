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
import android.util.SparseArray
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import si.inova.kotlinova.ui.state.StateSaverManager
import si.inova.kotlinova.ui.state.StateSavingComponent

class AdapterViewStateSaverTest {
    private lateinit var adapterView: AdapterView<Adapter>
    private lateinit var stateSaverManager: StateSaverManager
    private lateinit var adapterViewStateSaver: AdapterViewStateSaver

    @Before
    fun setUp() {
        stateSaverManager = mock()
        adapterView = mock()

        whenever(adapterView.id).thenReturn(VIEW_ID)

        val stateSavingComponent: StateSavingComponent = mock()
        whenever(stateSavingComponent.stateSaverManager).thenReturn(stateSaverManager)

        adapterViewStateSaver = AdapterViewStateSaver(stateSavingComponent, adapterView, SAVER_KEY)
    }

    @Test
    fun saving() {
        val testParcelable = StringParcelable("Test")
        whenever(adapterView.saveHierarchyState(any())).thenAnswer {
            @Suppress("UNCHECKED_CAST")
            val sparseArray = it.arguments[0] as SparseArray<Parcelable>
            sparseArray.put(VIEW_ID, testParcelable)
            Unit
        }

        assertEquals(testParcelable, adapterViewStateSaver.saveState())
    }

    @Test(expected = IllegalStateException::class)
    fun savingNoId() {
        whenever(adapterView.id).thenReturn(View.NO_ID)
        adapterViewStateSaver.saveState()
    }

    @Test
    fun restoring() {
        val testParcelable = StringParcelable("Test")
        whenever(stateSaverManager.getLastLoadedValue<Parcelable>(SAVER_KEY))
            .thenReturn(testParcelable)

        adapterViewStateSaver.notifyDataLoaded()

        verify(adapterView).restoreHierarchyState(argThat { testParcelable == get(VIEW_ID) })
    }

    @Test
    fun restoringTwice() {
        val testParcelable = StringParcelable("Test")
        whenever(stateSaverManager.getLastLoadedValue<Parcelable>(SAVER_KEY))
            .thenReturn(testParcelable)

        adapterViewStateSaver.notifyDataLoaded()
        adapterViewStateSaver.notifyDataLoaded()

        inOrder(adapterView) {
            verify(adapterView).restoreHierarchyState(argThat { testParcelable == get(VIEW_ID) })
            verifyNoMoreInteractions()
        }
    }

    @Test(expected = IllegalStateException::class)
    fun restoringNoId() {
        val testParcelable = StringParcelable("Test")
        whenever(stateSaverManager.getLastLoadedValue<Parcelable>(SAVER_KEY))
            .thenReturn(testParcelable)

        whenever(adapterView.id).thenReturn(View.NO_ID)
        adapterViewStateSaver.notifyDataLoaded()
    }
}

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

private const val SAVER_KEY = "AdapterView"
private const val VIEW_ID = 5