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
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.zhuinden.simplestack.History
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import si.inova.kotlinova.navigation.deeplink.HandleNewIntentDeepLinks
import si.inova.kotlinova.navigation.deeplink.MainDeepLinkHandler
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.navigation3.NavDisplay
import si.inova.kotlinova.navigation.sample.common.LocalSharedTransitionScope
import si.inova.kotlinova.navigation.sample.keys.MainScreenKey
import si.inova.kotlinova.navigation.sample.ui.theme.NavigationSampleTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey(MainActivity::class)
@Inject
class MainActivity(
   private val navigationInjectionFactory: NavigationInjection.Factory,
   private val mainDeepLinkHandler: MainDeepLinkHandler,
   private val navigationContext: NavigationContext,
) : FragmentActivity() {

   override fun onCreate(savedInstanceState: Bundle?) {
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
                     val backstack = navigationInjectionFactory.NavDisplay(
                        initialHistory = {
                           val initialBackstack = History.of(MainScreenKey)
                           (deepLinkTarget?.performNavigation(initialBackstack, navigationContext)?.newBackstack
                              ?: initialBackstack)
                        },
                        entryDecorators = listOf(
                           rememberSaveableStateHolderNavEntryDecorator(),
                           rememberViewModelStoreNavEntryDecorator()
                        )
                     )

                     mainDeepLinkHandler.HandleNewIntentDeepLinks(this@MainActivity, backstack)
                  }
               }
            }
         }
      }
   }
}
