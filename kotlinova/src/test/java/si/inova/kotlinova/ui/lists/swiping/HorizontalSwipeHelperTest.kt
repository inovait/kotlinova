package si.inova.kotlinova.ui.lists.swiping

import android.os.Build
import android.view.MotionEvent
import android.widget.FrameLayout
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import si.inova.kotlinova.testing.RobolectricTimeMachineRule
import si.inova.kotlinova.testing.advanceTime
import si.inova.kotlinova.time.TimeProvider

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
// Robolectric animations are broken in O and N. Use older Android Version
@Config(sdk = [(Build.VERSION_CODES.LOLLIPOP)])
class HorizontalSwipeHelperTest {
    @Mock
    lateinit var swipeListener: HorizontalSwipeHelper.Listener

    lateinit var view: FrameLayout
    lateinit var horizontalSwipeHelper: HorizontalSwipeHelper

    @get:Rule()
    val rule = RobolectricTimeMachineRule()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        view = FrameLayout(org.robolectric.RuntimeEnvironment.application)

        view.top = 0
        view.bottom = 500
        view.left = 0
        view.right = 500

        horizontalSwipeHelper = HorizontalSwipeHelper(view).apply {
            swipeListener = this@HorizontalSwipeHelperTest.swipeListener
        }
    }

    @Test
    fun dragWithFinger() {
        inOrder(swipeListener) {

            val downTime = TimeProvider.uptimeMillis()
            var event = MotionEvent.obtain(
                downTime,
                downTime,
                MotionEvent.ACTION_DOWN,
                500f,
                500f,
                0
            )
            assertFalse(horizontalSwipeHelper.onInterceptTouchEvent(event))

            assertEquals(0f, view.translationX)

            advanceTime(300)
            event = MotionEvent.obtain(
                downTime,
                TimeProvider.uptimeMillis(),
                MotionEvent.ACTION_MOVE,
                600f,
                500f,
                0
            )

            assertTrue(horizontalSwipeHelper.onInterceptTouchEvent(event))
            assertTrue(horizontalSwipeHelper.onTouchEvent(event))

            assertEquals(100f, view.translationX)
            verify(swipeListener).onMoved(100f)

            advanceTime(300)
            event = MotionEvent.obtain(
                downTime,
                TimeProvider.uptimeMillis(),
                MotionEvent.ACTION_MOVE,
                400f,
                500f,
                0
            )

            assertTrue(horizontalSwipeHelper.onInterceptTouchEvent(event))
            assertTrue(horizontalSwipeHelper.onTouchEvent(event))

            assertEquals(-100f, view.translationX)
            verify(swipeListener).onMoved(-100f)
        }
    }

    @Test
    fun returnonUpEventAfterDrag() {
        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            600f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(100)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            400f,
            500f,
            0
        )

        assertTrue(horizontalSwipeHelper.onInterceptTouchEvent(event))
        assertTrue(horizontalSwipeHelper.onTouchEvent(event))

        assertEquals(0f, view.translationX) // View returns
        verify(swipeListener).onMoved(0f)
    }

    @Test
    fun returnOnCancelEventAfterDrag() {
        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            600f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(100)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_CANCEL,
            400f,
            500f,
            0
        )

        assertTrue(horizontalSwipeHelper.onInterceptTouchEvent(event))
        assertTrue(horizontalSwipeHelper.onTouchEvent(event))

        assertEquals(0f, view.translationX, 0.01f) // View returns
        verify(swipeListener).onMoved(0f)
    }

    @Test
    fun cancelAnimationAfterTouchAgain() {
        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(100)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            600f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(100)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            400f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        // Pause robolectric to not finish animation in its entirety
        Robolectric.getForegroundThreadScheduler().pause()

        advanceTime(100)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            400f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        assertTrue(horizontalSwipeHelper.onInterceptTouchEvent(event))
        assertTrue(horizontalSwipeHelper.onTouchEvent(event))

        Robolectric.getForegroundThreadScheduler().unPause()

        assertNotEquals(0f, view.translationX, 0.01f)
    }

    @Test
    fun detectSwipeRight() {
        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(50)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            600f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(100)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            400f,
            500f,
            0
        )

        Robolectric.getForegroundThreadScheduler().pause()

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        verify(swipeListener, never()).onSwipedRight()

        Robolectric.getForegroundThreadScheduler().unPause()

        assertEquals(500f, view.translationX, 0.01f)

        verify(swipeListener).onSwipedRight()

        verify(swipeListener, never()).onSwipedLeft()
    }

    @Test
    fun detectSwipeLeft() {
        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(50)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            400f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(100)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            400f,
            500f,
            0
        )

        Robolectric.getForegroundThreadScheduler().pause()

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        verify(swipeListener, never()).onSwipedLeft()

        Robolectric.getForegroundThreadScheduler().unPause()

        assertEquals(-500f, view.translationX, 0.01f)

        verify(swipeListener).onSwipedLeft()

        verify(swipeListener, never()).onSwipedRight()
    }

    @Test
    fun stickToLeftAnchor() {
        horizontalSwipeHelper.leftAnchor = 50f
        horizontalSwipeHelper.rightAnchor = 50f

        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(400)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            600f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            600f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        assertEquals(50f, view.translationX, 0.01f)
        verify(swipeListener, never()).onSwipedLeft()
        verify(swipeListener, never()).onSwipedRight()
    }

    @Test
    fun returnToStartWhenDragEndsLeftFromLeftAnchor() {
        horizontalSwipeHelper.leftAnchor = 50f
        horizontalSwipeHelper.rightAnchor = 50f

        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(400)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            540f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            540f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        assertEquals(0f, view.translationX, 0.01f)
        verify(swipeListener, never()).onSwipedLeft()
        verify(swipeListener, never()).onSwipedRight()
    }

    @Test
    fun advanceRightDragToSwipe() {
        horizontalSwipeHelper.leftAnchor = 50f
        horizontalSwipeHelper.rightAnchor = 50f

        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(1000)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            860f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            860f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        assertEquals(500f, view.translationX, 0.01f)

        verify(swipeListener).onSwipedRight()
        verify(swipeListener, never()).onSwipedLeft()
    }

    @Test
    fun advanceRightDragToSwipeInTwoMovements() {
        horizontalSwipeHelper.leftAnchor = 50f
        horizontalSwipeHelper.rightAnchor = 50f

        var downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(1000)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            560f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            560f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        downTime = TimeProvider.uptimeMillis()
        event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            560f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(1000)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            860f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            860f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        //HACK: Robolectric animations are bugged
        // (https://github.com/robolectric/robolectric/issues/1809)
        // (they do not finish if you activate two animations sequentially)
        // Manually check animation state
        assertEquals(500f, horizontalSwipeHelper.currentAnimation?.targetX)

        horizontalSwipeHelper.currentAnimation?.onAnimationEnd(null)
        verify(swipeListener).onSwipedRight()
        verify(swipeListener, never()).onSwipedLeft()
    }

    @Test
    fun stickToRightAnchor() {
        horizontalSwipeHelper.leftAnchor = 50f
        horizontalSwipeHelper.rightAnchor = 50f

        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(400)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            400f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            400f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        assertEquals(-50f, view.translationX, 0.01f)
        verify(swipeListener, never()).onSwipedLeft()
        verify(swipeListener, never()).onSwipedRight()
    }

    @Test
    fun returnToStartWhenDragEndsRightFromRightAnchor() {
        horizontalSwipeHelper.leftAnchor = 50f
        horizontalSwipeHelper.rightAnchor = 50f

        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(400)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            460f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            460f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        assertEquals(0f, view.translationX, 0.01f)
        verify(swipeListener, never()).onSwipedLeft()
        verify(swipeListener, never()).onSwipedRight()
    }

    @Test
    fun advanceLeftDragToSwipe() {
        horizontalSwipeHelper.leftAnchor = 50f
        horizontalSwipeHelper.rightAnchor = 50f

        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(1000)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            140f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            140f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        assertEquals(-500f, view.translationX, 0.01f)

        verify(swipeListener).onSwipedLeft()
        verify(swipeListener, never()).onSwipedRight()
    }

    @Test
    fun advanceLeftDragToSwipeWithoutAnchors() {
        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(1000)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            140f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            140f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        assertEquals(-500f, view.translationX, 0.01f)

        verify(swipeListener).onSwipedLeft()
        verify(swipeListener, never()).onSwipedRight()
    }

    @Test
    fun advanceRightDragToSwipeWithoutAnchors() {
        val downTime = TimeProvider.uptimeMillis()
        var event = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            500f,
            500f,
            0
        )
        horizontalSwipeHelper.onInterceptTouchEvent(event)

        advanceTime(1000)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            860f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        advanceTime(300)
        event = MotionEvent.obtain(
            downTime,
            TimeProvider.uptimeMillis(),
            MotionEvent.ACTION_UP,
            860f,
            500f,
            0
        )

        horizontalSwipeHelper.onInterceptTouchEvent(event)
        horizontalSwipeHelper.onTouchEvent(event)

        assertEquals(500f, view.translationX, 0.01f)

        verify(swipeListener).onSwipedRight()
        verify(swipeListener, never()).onSwipedLeft()
    }
}