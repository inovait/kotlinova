package si.inova.kotlinova.preferences

import android.content.SharedPreferences
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter

/**
 * Get observable that emits keys that got updated through shared preferences
 */
fun SharedPreferences.updatesObservable(): Flowable<String> {
    return Flowable.create({ emitter: FlowableEmitter<String> ->
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                emitter.onNext(key)
            }

        registerOnSharedPreferenceChangeListener(listener)

        emitter.setCancellable {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }, BackpressureStrategy.BUFFER)
}