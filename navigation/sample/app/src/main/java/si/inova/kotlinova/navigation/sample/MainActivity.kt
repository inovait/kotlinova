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

package si.inova.kotlinova.navigation.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.zhuinden.simplestack.History
import si.inova.kotlinova.navigation.NavigationSubComponent
import si.inova.kotlinova.navigation.deeplink.HandleNewIntentDeepLinks
import si.inova.kotlinova.navigation.deeplink.MainDeepLinkHandler
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.sample.common.LocalSharedTransitionScope
import si.inova.kotlinova.navigation.sample.keys.MainScreenKey
import si.inova.kotlinova.navigation.sample.ui.theme.NavigationSampleTheme
import si.inova.kotlinova.navigation.simplestack.RootNavigationContainer

@OptIn(ExperimentalSharedTransitionApi::class)
class MainActivity : FragmentActivity() {
   private lateinit var navigationInjectionFactory: NavigationInjection.Factory
   private lateinit var mainDeepLinkHandler: MainDeepLinkHandler
   private lateinit var navigationContext: NavigationContext

   override fun onCreate(savedInstanceState: Bundle?) {
      val navigationComponent = (application as NavigationSampleApplication).component.createNavigationComponent()

      navigationInjectionFactory = navigationComponent.getNavigationInjectionFactory()
      mainDeepLinkHandler = navigationComponent.getMainDeepLinkHandler()
      navigationContext = navigationComponent.getNavigationContext()

      super.onCreate(savedInstanceState)

      val deepLinkTarget = if (savedInstanceState == null) {
         intent?.data?.let { mainDeepLinkHandler.handleDeepLink(it, startup = true) }
      } else {
         null
      }

      setContent {
         NavigationSampleTheme {
            SharedTransitionLayout {
               CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                  Surface(
                     modifier = Modifier.fillMaxSize(),
                     color = MaterialTheme.colorScheme.background
                  ) {
                     val backstack = navigationInjectionFactory.RootNavigationContainer {
                        val initialBackstack = History.of(MainScreenKey)
                        (deepLinkTarget?.performNavigation(initialBackstack, navigationContext)?.newBackstack ?: initialBackstack)
                     }

                     mainDeepLinkHandler.HandleNewIntentDeepLinks(this@MainActivity, backstack)
                  }
               }
            }
         }
      }
   }
}
