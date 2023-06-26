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

package si.inova.kotlinova.navigation.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setContainerAvailable
import com.zhuinden.simplestack.ScopedServices
import si.inova.kotlinova.navigation.fragment.util.requireActivity
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.ScopedService
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.random.Random

/**
 * A navigation screen that displays a fragment
 */
abstract class FragmentScreen<K>(
   private val scopeExitListener: ScopeExitListener
) : Screen<K>() where K : FragmentScreenKey, K : ScreenKey {
   @Composable
   override fun Content(key: K) {
      val activity = LocalContext.current.requireActivity() as FragmentActivity

      val fragmentViewId = rememberSaveable { Random.nextInt(MAX_VIEW_ID) }

      AndroidView({ context ->
         FragmentContainerView(context).apply { id = fragmentViewId }
      })

      DisposableEffect(key, fragmentViewId) {
         val fragmentManager = activity.supportFragmentManager
         if (fragmentManager.isStateSaved) {
            // Fragment manager's state has already been saved. If we do anything, we will crash
            // Exit here, activity is likely being closed anyway, so Fragment will re-appear
            // on next open
            return@DisposableEffect onDispose { }
         }

         var currentFragment = fragmentManager.findFragmentByTag(key.tag)

         if (currentFragment == null) {
            currentFragment = createFragment(key, fragmentManager)

            fragmentManager
               .beginTransaction()
               .replace(fragmentViewId, currentFragment, key.tag)
               .commitNow()
         }

         scopeExitListener.fragments[key.javaClass] = WeakReference(fragmentManager to currentFragment)

         if (currentFragment.isDetached) {
            fragmentManager
               .beginTransaction()
               .attach(currentFragment)
               .commit()
         }

         fragmentManager.setContainerAvailable(activity.findViewById(fragmentViewId))

         onDispose {
            if (!fragmentManager.isStateSaved) {
               fragmentManager
                  .beginTransaction()
                  .detach(currentFragment)
                  .commit()
            }
         }
      }
   }

   abstract fun createFragment(key: K, fragmentManager: FragmentManager): Fragment
}

class ScopeExitListener @Inject constructor() : ScopedServices.Registered, ScopedService {
   val fragments = HashMap<Class<ScreenKey>, WeakReference<Pair<FragmentManager, Fragment>>>()

   override fun onServiceRegistered() {}

   override fun onServiceUnregistered() {
      fragments.values.mapNotNull { it.get() }.forEach { (fragmentManager, fragment) ->
         if (!fragment.isStateSaved) {
            fragmentManager.beginTransaction()
               .remove(fragment)
               .commit()
         }
      }
   }
}

private const val MAX_VIEW_ID = 0x00FFFFFF
