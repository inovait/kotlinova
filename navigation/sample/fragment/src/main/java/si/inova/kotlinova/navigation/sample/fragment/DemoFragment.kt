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

package si.inova.kotlinova.navigation.sample.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import si.inova.kotlinova.navigation.fragment.FragmentScreen
import si.inova.kotlinova.navigation.fragment.ScopeExitListener
import si.inova.kotlinova.navigation.sample.keys.DemoFragmentScreenKey

class DemoFragment : Fragment() {
   @SuppressLint("SetTextI18n")
   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
      return TextView(requireContext()).apply {
         text = "Hello From Fragment. Got argument: ${arguments?.getInt(ARGUMENT_INPUT)}"
      }
   }

   companion object {
      fun newInstance(inputNumber: Int): DemoFragment {
         return DemoFragment().apply {
            arguments = bundleOf(ARGUMENT_INPUT to inputNumber)
         }
      }

      private const val ARGUMENT_INPUT = "input"
   }
}

class DemoFragmentScreen(scopeExitListener: ScopeExitListener) : FragmentScreen<DemoFragmentScreenKey>(scopeExitListener) {
   override fun createFragment(key: DemoFragmentScreenKey, fragmentManager: FragmentManager): Fragment {
      return DemoFragment.newInstance(key.inputNumber)
   }
}
