/*
 * Copyright 2023 INOVA IT d.o.o.
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

package si.inova.kotlinova.navigation.testutils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent
import com.zhuinden.simplestack.Backstack
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.simplestack.RootNavigationContainer

@MergeComponent(OuterNavigationScope::class)
interface TestComponent {
   fun navigationFactory(): NavigationInjection.Factory
}

@Module
@ContributesTo(OuterNavigationScope::class)
class TestModule {
   @Provides
   fun provideCoroutineScope() = CoroutineScope(Dispatchers.Unconfined + Job())
}

fun ComposeContentTestRule.insertTestNavigation(vararg initialHistory: ScreenKey): Backstack {
   val navigation = DaggerTestComponent.create().navigationFactory()
   lateinit var backstack: Backstack

   setContent {
      Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
         backstack = navigation.RootNavigationContainer { initialHistory.toList() }
      }
   }

   waitForIdle()

   return backstack
}

fun StateRestorationTester.insertTestNavigation(composeTestRule: ComposeTestRule, vararg initialHistory: ScreenKey): Backstack {
   val navigation = DaggerTestComponent.create().navigationFactory()
   lateinit var backstack: Backstack

   setContent {
      Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
         backstack = navigation.RootNavigationContainer { initialHistory.toList() }
      }
   }

   composeTestRule.waitForIdle()

   return backstack
}
