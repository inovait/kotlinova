/*
 * Copyright 2026 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.kotlinova.navigation.services

import android.os.Parcelable
import com.zhuinden.simplestack.Bundleable
import com.zhuinden.statebundle.StateBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.util.set
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Scoped service that can save its state and gets recreated after process kill.
 * To use, create properties using "by saved" delegation. For example:
 *
 * ```
 * class MyService: SaveableScopedService() {
 *   val savedNumber by saved(10)
 * }
 * ```
 *
 * @param [coroutineScope] See documentation for [CoroutineScopedService]
 */
abstract class SaveableScopedService(
   coroutineScope: CoroutineScope,
) : CoroutineScopedService(coroutineScope), Bundleable {
   protected var bundle = StateBundle()

   /**
    * Property that gets automatically saved and restored when this ScopedService gets recreated after a process kill.
    *
    * @param defaultValue Default value that property has. This value is only saved on the first read.
    */
   fun <T> saved(
      defaultValue: () -> T,
   ): ReadWriteProperty<SaveableScopedService, T> {
      return StateSavedProperty(defaultValue)
   }

   /**
    * Flow of a property that gets automatically saved and restored when this ScopedService gets recreated after a process kill.
    *
    * @param defaultValue Default value that property has. This value is only saved on the first read.
    */
   fun <T> savedFlow(
      defaultValue: () -> T,
   ): ReadOnlyProperty<SaveableScopedService, MutableStateFlow<T>> {
      return StateSavedFlowProperty(defaultValue)
   }

   /**
    * Property that gets automatically saved and restored when this ScopedService gets recreated after a process kill.
    *
    * @param defaultValue Default value that property has. This value is only saved on the first read.
    */
   fun <T> saved(
      defaultValue: T,
   ): ReadWriteProperty<SaveableScopedService, T> = saved { defaultValue }

   /**
    * Flow of a property that gets automatically saved and restored when this ScopedService gets recreated after a process kill.
    *
    * @param defaultValue Default value that property has. This value is only saved on the first read.
    */
   fun <T> savedFlow(
      defaultValue: T,
   ): ReadOnlyProperty<SaveableScopedService, MutableStateFlow<T>> = savedFlow { defaultValue }

   override fun toBundle(): StateBundle {
      return bundle
   }

   override fun fromBundle(bundle: StateBundle?) {
      if (bundle != null) {
         this.bundle = bundle
      }
   }

   private class StateSavedProperty<T>(
      private val defaultValue: () -> T,
   ) : ReadWriteProperty<SaveableScopedService, T> {
      var value: T? = null
      var initialized = false

      @Suppress("UNCHECKED_CAST")
      override fun getValue(thisRef: SaveableScopedService, property: KProperty<*>): T {
         if (!initialized) {
            if (thisRef.bundle.containsKey(property.name)) {
               val savedValue = thisRef.bundle.get(property.name)!! as T
               value = if (savedValue == NullValue) {
                  null
               } else {
                  savedValue
               }
            } else {
               val default = defaultValue()
               value = default
               save(default, thisRef, property)
            }

            initialized = true
         }

         return value as T
      }

      override fun setValue(thisRef: SaveableScopedService, property: KProperty<*>, value: T) {
         initialized = true
         this.value = value

         save(value, thisRef, property)
      }

      private fun save(
         value: T,
         thisRef: SaveableScopedService,
         property: KProperty<*>,
      ) {
         thisRef.bundle[property.name] = if (value == null) NullValue else value
      }
   }

   private class StateSavedFlowProperty<T>(
      private val defaultValue: () -> T,
   ) : ReadOnlyProperty<SaveableScopedService, MutableStateFlow<T>> {
      var flow: MutableStateFlow<T>? = null

      override fun getValue(thisRef: SaveableScopedService, property: KProperty<*>): MutableStateFlow<T> {
         val existingFlow = flow
         if (existingFlow != null) {
            return existingFlow
         }

         @Suppress("UNCHECKED_CAST")
         val initialValue = if (thisRef.bundle.containsKey(property.name)) {
            val savedValue = thisRef.bundle.get(property.name) as T
            if (savedValue == NullValue) {
               null as T
            } else {
               savedValue
            }
         } else {
            defaultValue()
         }

         val newFlow = MutableStateFlow<T>(initialValue)
         newFlow.startObserving(thisRef, property)

         this.flow = newFlow
         return newFlow
      }

      private fun StateFlow<T>.startObserving(
         thisRef: SaveableScopedService,
         property: KProperty<*>,
      ) {
         thisRef.coroutineScope.launch {
            collect { value ->
               thisRef.bundle[property.name] = if (value == null) NullValue else value
            }
         }
      }
   }
}

@Parcelize
private data object NullValue : Parcelable
