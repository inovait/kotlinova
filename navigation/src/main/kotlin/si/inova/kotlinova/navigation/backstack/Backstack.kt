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

package si.inova.kotlinova.navigation.backstack

import android.os.Parcelable
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.di.ScreenRegistry
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.services.ScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import kotlin.reflect.KClass

class Backstack(
   initialKeys: List<ScreenKey>,
   private val scopedServicesFactories: Lazy<Map<KClass<*>, Provider<out Any>>>,
   private val screenRegistry: Lazy<ScreenRegistry>,
   private val parent: Backstack?,
   private val parentScope: String?,
   private val globalServices: List<KClass<*>> = emptyList(),
) {
   private val _backstack = MutableStateFlow(initialKeys)

   @Suppress("MemberNameEqualsClassName") //  Fine in this instance
   val backstack: StateFlow<List<ScreenKey>>
      get() = _backstack

   private val scopes = LinkedHashMap<String, Map<KClass<*>, Any>>()

   fun init(savedState: SavedState?) {
      if (scopes.isNotEmpty()) {
         // Class has already been initialized. Likely, the Composable holding the Backstack got killed, but the
         // ViewModel still survived.
         return
      }
      createScope(GLOBAL_SERVICE_SCOPE_TAG, globalServices, callOnRegistered = false)

      val restoredBackstack = if (savedState != null) {
         savedState.read {
            getParcelable<SavedBackstack>(SAVED_STATE_KEY_BACKSTACK).keys
         }
      } else {
         backstack.value
      }

      updateBackstack(restoredBackstack, callOnRegisteredForServices = false)

      for ((scopeKey, scope) in scopes) {
         for ((serviceKey, service) in scope) {
            if (savedState != null && service is ScopedService.Saveable) {
               val serviceState = savedState.read {
                  getSavedStateOrNull(getServiceSavedStateKey(scopeKey, serviceKey))
               }

               serviceState?.let { service.restoreState(it) }
            }

            if (service is ScopedService.Registered) {
               service.onServiceRegistered()
            }
         }
      }
   }

   fun saveState(): SavedState {
      return savedState {
         putParcelable(SAVED_STATE_KEY_BACKSTACK, SavedBackstack(_backstack.value))

         for ((scopeKey, scope) in scopes) {
            for ((serviceKey, service) in scope) {
               if (service is ScopedService.Saveable) {
                  putSavedState(getServiceSavedStateKey(scopeKey, serviceKey), service.saveState())
               }
            }
         }
      }
   }

   fun onDestroy() {
      for (scope in scopes.values) {
         for (service in scope.values) {
            if (service is ScopedService.Registered) {
               service.onServiceUnregistered()
            }
         }
      }
   }

   fun updateBackstack(newBackstack: List<ScreenKey>) {
      updateBackstack(newBackstack, callOnRegisteredForServices = true)
   }

   private fun updateBackstack(newBackstack: List<ScreenKey>, callOnRegisteredForServices: Boolean) {
      val scopesIterator = scopes.entries.iterator()
      while (scopesIterator.hasNext()) {
         val (scope, services) = scopesIterator.next()
         if (scope != GLOBAL_SERVICE_SCOPE_TAG && !newBackstack.any { it.getScopeTag() == scope }) {
            for (service in services.values) {
               if (service is ScopedService.Registered) {
                  service.onServiceUnregistered()
               }
            }

            scopesIterator.remove()
         }
      }

      for (key in newBackstack) {
         val scopeTag = key.getScopeTag()
         if (!scopes.contains(scopeTag)) {
            createScope(
               scopeTag,
               screenRegistry.value.getRequiredScopedServices(key),
               key,
               callOnRegisteredForServices
            )
         }
      }

      _backstack.value = newBackstack
   }

   fun <T : Any> lookupService(type: KClass<T>): T? {
      @Suppress("UNCHECKED_CAST")
      return scopes.values.firstNotNullOfOrNull { it[type] } as T?
         ?: lookupFromParentService(type)
   }

   fun <T : Any> lookupFromScope(scope: String, type: KClass<T>): T? {
      @Suppress("UNCHECKED_CAST")
      return scopes.get(scope)?.get(type) as T?
         ?: lookupFromParentService(type)
   }

   private fun <T : Any> lookupFromParentService(type: KClass<T>): T? {
      if (parent == null) return null

      return if (parentScope == null) {
         parent.lookupService(type)
      } else {
         parent.lookupFromScope(parentScope, type)
      }
   }

   private fun createScope(
      scope: String,
      classes: List<KClass<*>>,
      creatingKey: ScreenKey? = null,
      callOnRegistered: Boolean = true,
   ) {
      val createdServices = classes.associateWith { key ->
         scopedServicesFactories.value[key]?.invoke() ?: error(
            "Scoped service ${key.qualifiedName ?: "NO_NAME"} is missing from the injection. " +
               "Did you add @ContributesScopedService annotation to it?"
         )
      }

      for (service in createdServices.values) {
         if (service is SingleScreenViewModel<*>) {
            @Suppress("UNCHECKED_CAST")
            (service as SingleScreenViewModel<ScreenKey>).key = creatingKey
               ?: error("SingleScreenViewModel $service cannot be used as a global service")
         }

         if (callOnRegistered && service is ScopedService.Registered) {
            service.onServiceRegistered()
         }
      }

      val previousScope = scopes.put(scope, createdServices)
      if (previousScope != null) {
         error("Double scope creation: $scope")
      }
   }

   private fun getServiceSavedStateKey(scopeKey: String, serviceCls: KClass<*>): String {
      val serviceName = serviceCls.qualifiedName ?: error("Unnamed classes are not supported as scoped services")
      return "SERVICE_${scopeKey}_$serviceName"
   }

   @Parcelize
   private data class SavedBackstack(val keys: List<ScreenKey>) : Parcelable

   companion object {
      const val GLOBAL_SERVICE_SCOPE_TAG = "SPECIAL_GLOBAL_SERVICES"

      private const val SAVED_STATE_KEY_BACKSTACK = "BACKSTACK"
   }
}
