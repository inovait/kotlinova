/*
 * Copyright 2024 INOVA IT d.o.o.
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

package si.inova.kotlinova.navigation.di

import android.annotation.SuppressLint
import android.os.Parcel
import androidx.compose.runtime.Composable
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.Provides
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.ScopedService
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

@ContributesTo(BackstackScope::class)
@Component
interface EmptyNavigationComponent {
   val screenRegistrationMultibinds: Map<Class<out ScreenKey>, ScreenRegistration<*>>
   val screenFactoryMultibinds: Map<Class<out Screen<out ScreenKey>>, ScreenFactory<*>>
   val scopedService: Map<Class<out ScopedService>, () -> ScopedService>

   // Dummy registrations that will never be used but can be used to create multibindings.
   // Workaround for a lack of https://github.com/evant/kotlin-inject/issues/249

   @Provides
   @IntoMap
   fun provideDummyScreenRegistration(): Pair<Class<out ScreenKey>, ScreenRegistration<*>> =
      DummyClass::class.java to ScreenRegistration(DummyScreen::class.java)

   @Provides
   @IntoMap
   fun provideDummyScreenFactory(): Pair<Class<out Screen<out ScreenKey>>, ScreenFactory<*>> =
      DummyScreen::class.java to ScreenFactory(
         emptyList(),
         emptyList(),
         { _, _ -> throw UnsupportedOperationException("This class should never be actually used") })

   @Provides
   @IntoMap
   fun provideDummyScopedService(): Pair<Class<out ScopedService>, () -> ScopedService> =
      DummyClass::class.java to { DummyClass() }
}

/**
 */
@SuppressLint("ParcelCreator")
@Suppress("ExceptionRaisedInUnexpectedLocation")
private class DummyClass : ScreenKey(), ScopedService {
   override fun toString(): String {
      throw UnsupportedOperationException("This class should never be actually used")
   }

   override fun equals(other: Any?): Boolean {
      throw UnsupportedOperationException("This class should never be actually used")
   }

   override fun hashCode(): Int {
      throw UnsupportedOperationException("This class should never be actually used")
   }

   override fun describeContents(): Int {
      throw UnsupportedOperationException("This class should never be actually used")
   }

   override fun writeToParcel(dest: Parcel, flags: Int) {
      throw UnsupportedOperationException("This class should never be actually used")
   }
}

private class DummyScreen : Screen<DummyClass>() {
   @Composable
   override fun Content(key: DummyClass) {
      throw UnsupportedOperationException("This class should never be actually used")
   }
}
