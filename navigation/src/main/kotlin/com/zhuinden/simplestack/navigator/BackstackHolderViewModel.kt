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

package com.zhuinden.simplestack.navigator

import androidx.lifecycle.ViewModel
import com.zhuinden.simplestack.BackHandlingModel
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.GlobalServices
import com.zhuinden.simplestack.KeyFilter
import com.zhuinden.simplestack.KeyParceler
import com.zhuinden.simplestack.ScopedServices

/**
 * This is a copy from https://github.com/Zhuinden/simple-stack-compose-integration/pull/22/ until
 * it gets merged.
 */
internal class BackstackHolderViewModel : ViewModel() {
   private val backstacks = HashMap<String, Backstack>()

   fun getBackstack(id: String): Backstack? {
      return backstacks[id]
   }

   fun createInitializer(id: String) = object : ComposeNavigatorInitializer {
      override fun createBackstack(
         initialKeys: List<*>,
         keyFilter: KeyFilter,
         keyParceler: KeyParceler,
         stateClearStrategy: Backstack.StateClearStrategy,
         scopedServices: ScopedServices?,
         globalServices: GlobalServices?,
         parent: Backstack?,
         parentScope: String?,
         globalServicesFactory: GlobalServices.Factory?,
      ): Backstack {
         val backstack = Backstack()

         backstack.setBackHandlingModel(BackHandlingModel.AHEAD_OF_TIME)
         backstack.setKeyFilter(keyFilter)
         backstack.setKeyParceler(keyParceler)
         backstack.setStateClearStrategy(stateClearStrategy)
         backstack.setParentServices(parent, parentScope)

         scopedServices?.let { backstack.setScopedServices(it) }
         globalServices?.let { backstack.setGlobalServices(it) }
         globalServicesFactory?.let { backstack.setGlobalServices(it) }

         backstack.setup(initialKeys)
         backstacks[id] = backstack

         return backstack
      }
   }

   override fun onCleared() {
      for (backstack in backstacks.values) {
         backstack.finalizeScopes()
      }
   }
}
