/*
 * Copyright 2025 INOVA IT d.o.o.
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

package si.inova.kotlinova.navigation.sample

import android.app.Activity
import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import kotlin.reflect.KClass

@DependencyGraph(AppScope::class, additionalScopes = [OuterNavigationScope::class])
@SingleIn(AppScope::class)
interface AppGraph {
   /**
    * A multibinding map of activity classes to their providers accessible for
    * [MetroAppComponentFactory].
    */
   @Multibinds
   val activityProviders: Map<KClass<out Activity>, Provider<Activity>>

   @Provides
   fun provideCoroutineScope(): CoroutineScope {
      // Provide coroutine scope for the ViewModels. This is injected to ensure tests can swap it out with the test scope

      return CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
   }

   @DependencyGraph.Factory
   interface Factory {
      fun create(
         @Provides
         application: Application
      ): AppGraph
   }
}
