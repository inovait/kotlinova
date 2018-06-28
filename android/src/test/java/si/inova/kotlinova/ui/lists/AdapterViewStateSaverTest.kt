package si.inova.kotlinova.ui.lists

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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import si.inova.kotlinova.ui.state.StateSaverManager
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
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

private const val SAVER_KEY = "AdapterView"
private const val VIEW_ID = 5