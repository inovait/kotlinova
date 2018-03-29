package si.inova.kotlinova.ui.views

import android.app.Activity
import android.os.Bundle
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import si.inova.kotlinova.testing.advanceTime

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
class FullScreenProgressBarTest {
    private lateinit var fullScreenProgressBar: FullScreenProgressBar
    private lateinit var activity: ActivityController<HolderActivity>

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(HolderActivity::class.java)
                .create()
                .start()
                .resume()

        fullScreenProgressBar = activity.get().fullScreenProgressBar
    }

    @Test
    fun showAfterDelay() {
        fullScreenProgressBar.show()
        assertEquals(View.GONE, fullScreenProgressBar.visibility)

        advanceTime(100)
        assertEquals(View.GONE, fullScreenProgressBar.visibility)

        advanceTime(500)
        assertEquals(View.VISIBLE, fullScreenProgressBar.visibility)
    }

    @Test
    fun hideAfterDelay() {
        fullScreenProgressBar.show()
        advanceTime(600)

        advanceTime(600)
        fullScreenProgressBar.hide()

        advanceTime(260) // Wait for animation
        assertEquals(View.GONE, fullScreenProgressBar.visibility)
    }

    @Test
    fun doNotHideImmediately() {
        fullScreenProgressBar.show()
        advanceTime(501)

        fullScreenProgressBar.hide()

        advanceTime(260) // Wait for potential animation
        assertEquals(View.VISIBLE, fullScreenProgressBar.visibility)

        advanceTime(500)

        advanceTime(260) // Wait for animation
        assertEquals(View.GONE, fullScreenProgressBar.visibility)
    }

    @Test
    fun doNotShowAfterQuickHide() {
        fullScreenProgressBar.show()
        advanceTime(300)
        fullScreenProgressBar.hide()
        advanceTime(500)

        advanceTime(260) // Wait for animation
        assertEquals(View.GONE, fullScreenProgressBar.visibility)
    }

    private class HolderActivity : Activity() {
        lateinit var fullScreenProgressBar: FullScreenProgressBar

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            fullScreenProgressBar = FullScreenProgressBar(this)
            setContentView(fullScreenProgressBar)
        }
    }
}