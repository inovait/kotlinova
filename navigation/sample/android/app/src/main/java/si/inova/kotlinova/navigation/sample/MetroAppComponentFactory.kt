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
import android.content.Intent
import androidx.annotation.Keep
import androidx.core.app.AppComponentFactory
import dev.zacsweers.metro.Provider
import kotlin.reflect.KClass

/**
 * An [AppComponentFactory] that uses Metro for constructor injection of Activities.
 *
 * If you have minSdk < 28, you can fall back to using member injection on Activities or (better)
 * use an architecture that abstracts the Android framework components away.
 *
 * Adapted from https://github.com/ZacSweers/metro/blob/main/samples/android-app/src/main/kotlin/dev/zacsweers/metro/sample/android/MetroAppComponentFactory.kt
 */
@Keep
class MetroAppComponentFactory : AppComponentFactory() {

   private inline fun <reified T : Any> getInstance(
      cl: ClassLoader,
      className: String,
      providers: Map<KClass<out T>, Provider<T>>,
   ): T? {
      val clazz = Class.forName(className, false, cl).asSubclass(T::class.java)
      val modelProvider = providers[clazz.kotlin] ?: return null
      return modelProvider()
   }

   override fun instantiateActivityCompat(
      cl: ClassLoader,
      className: String,
      intent: Intent?,
   ): Activity {
      return getInstance(cl, className, activityProviders)
         ?: super.instantiateActivityCompat(cl, className, intent)
   }

   override fun instantiateApplicationCompat(cl: ClassLoader, className: String): Application {
      val app = super.instantiateApplicationCompat(cl, className)
      activityProviders = (app as NavigationSampleApplication).appGraph.activityProviders
      return app
   }

   // AppComponentFactory can be created multiple times
   companion object {
      private lateinit var activityProviders: Map<KClass<out Activity>, Provider<Activity>>
   }
}
