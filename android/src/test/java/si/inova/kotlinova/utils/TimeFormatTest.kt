package si.inova.kotlinova.utils

import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * @author Matej Drobnic
 */
@Ignore
@RunWith(RobolectricTestRunner::class)
class TimeFormatTest {
    @Test
    fun toHoursMinutesSeconds() {
        val context = RuntimeEnvironment.application

        assertEquals("00:00:00", TimeFormat.toHoursMinutesSeconds(0, context))
        assertEquals("01:00:00", TimeFormat.toHoursMinutesSeconds(3600, context))
        assertEquals("00:01:00", TimeFormat.toHoursMinutesSeconds(60, context))
        assertEquals("00:00:01", TimeFormat.toHoursMinutesSeconds(1, context))
        assertEquals("00:02:38", TimeFormat.toHoursMinutesSeconds(158, context))
        assertEquals("150:00:00", TimeFormat.toHoursMinutesSeconds(540000, context))
        assertEquals("01:30:32", TimeFormat.toHoursMinutesSeconds(5432, context))

        assertEquals(
            "02:38",
            TimeFormat.toHoursMinutesSeconds(158, context, alwaysShowHours = false)
        )

        assertEquals(
            "01:30:32",
            TimeFormat.toHoursMinutesSeconds(5432, context, alwaysShowHours = false)
        )
    }

    @Test
    fun toMinutesSeconds() {
        val context = RuntimeEnvironment.application

        assertEquals("00:00", TimeFormat.toMinutesSeconds(0, context))
        assertEquals("01:00", TimeFormat.toMinutesSeconds(60, context))
        assertEquals("00:01", TimeFormat.toMinutesSeconds(1, context))
        assertEquals("02:38", TimeFormat.toMinutesSeconds(158, context))
        assertEquals("150:00", TimeFormat.toMinutesSeconds(9000, context))
        assertEquals("01:30", TimeFormat.toMinutesSeconds(90, context))
    }
}