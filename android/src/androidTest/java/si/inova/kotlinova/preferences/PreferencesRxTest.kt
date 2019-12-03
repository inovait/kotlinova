package si.inova.kotlinova.preferences

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.reactivex.observers.BaseTestConsumer
import junit.framework.Assert.assertFalse
import org.junit.After
import org.junit.Test
import si.inova.kotlinova.testing.fakes.MemorySharedPreferences

class PreferencesRxTest {
    private val preferences = InstrumentationRegistry.getInstrumentation()
        .targetContext.getSharedPreferences("test", Context.MODE_PRIVATE)

    @After
    fun tearDown() {
        preferences.edit().clear().apply()
    }

    @Test
    fun receiveUpdates() {
        val observable = preferences.updatesObservable()

        val subscriber = observable.test()

        preferences.edit()
            .putInt("A", 10)
            .apply()

        preferences.edit()
            .putInt("B", 20)
            .apply()

        preferences.edit()
            .putInt("C", 30)
            .apply()

        subscriber.awaitCount(
            3,
            BaseTestConsumer.TestWaitStrategy.SLEEP_10MS,
            5_000
        )
        subscriber.assertValues("A", "B", "C")
    }

    @Test
    fun unregisterOnDispose() {
        val memoryPreferences = MemorySharedPreferences()
        val observable = memoryPreferences.updatesObservable()

        val subscriber = observable.test()

        subscriber.dispose()

        assertFalse(memoryPreferences.hasListeners)
    }
}