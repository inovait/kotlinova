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

package si.inova.kotlinova.navigation.tests.internalmodule

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import si.inova.kotlinova.navigation.di.BackstackScope
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.ScopedService

@Serializable
data object InternalScreenKey : ScreenKey()

@InjectNavigationScreen
@ContributesScreenBinding
internal class InternalScreen : Screen<InternalScreenKey>() {
   @Composable
   override fun Content(key: InternalScreenKey) {
      Text("Hello from Internal Screen")
   }
}

@Serializable
data object InternalScreenWithBasicServiceKey : ScreenKey()

@InjectNavigationScreen
internal class InternalScreenWithBasicService(
   private val service: BasicInternalService,
) : Screen<InternalScreenWithBasicServiceKey>() {
   @Composable
   override fun Content(key: InternalScreenWithBasicServiceKey) {
      Column {
         Text("Number: ${service.data.collectAsStateWithLifecycle().value}")
         Button(onClick = { service.data.update { it + 1 } }) {
            Text("Increase")
         }
      }
   }
}

@Serializable
data object InternalScreenWithBasicServiceInterfaceKey : ScreenKey()

@InjectNavigationScreen
internal class InternalScreenWithBasicServiceInterface(
   private val service: BasicInternalServiceWithInterface,
) : Screen<InternalScreenWithBasicServiceInterfaceKey>() {
   @Composable
   override fun Content(key: InternalScreenWithBasicServiceInterfaceKey) {
      Column {
         Text("Number: ${service.data.collectAsStateWithLifecycle().value}")
         Button(onClick = { service.data.update { it + 1 } }) {
            Text("Increase")
         }
      }
   }
}

@Inject
@ContributesIntoMap(BackstackScope::class, binding = binding<ScopedService>())
@ClassKey(BasicInternalService::class)
internal class BasicInternalService : ScopedService, ScopedService.Registered {
   val data = MutableStateFlow(0)
}

internal interface BasicInternalServiceWithInterface : ScopedService {
   val data: MutableStateFlow<Int>
}

@ContributesIntoMap(BackstackScope::class, binding = binding<ScopedService>())
@ClassKey(BasicInternalServiceWithInterface::class)
@Inject
internal class BasicInternalServiceWithInterfaceImpl : BasicInternalServiceWithInterface {
   override val data = MutableStateFlow(0)
}
