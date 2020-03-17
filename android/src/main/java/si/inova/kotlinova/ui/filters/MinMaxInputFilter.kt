/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.ui.filters

import android.text.InputFilter
import android.text.Spanned

/**
 * Created on 14.9.2017.
 * This input filter forces the user to type a number in between the interval [min, max]
 *
 * @author Bojan Kseneman, adapted by Matej Drobnic
 */

class MinMaxInputFilter
/**
 * Min max filter for Edit text
 *
 * @param min min allowed number
 * @param max max allowed number
 */
(private val min: Double, private val max: Double) : InputFilter {
    private val allowSignedNumbers: Boolean = min < 0

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        var returnValue: String? = ""
        try {
            var inputText = dest.toString() + source.toString()
            if (inputText == "-" && allowSignedNumbers) {
                returnValue = null
            } else {
                inputText = inputText.replace(",".toRegex(), ".")
                val input = java.lang.Double.parseDouble(inputText)
                if (isInRange(input)) {
                    returnValue = null
                }
            }
        } catch (nfe: NumberFormatException) {
            // Don't allow characters or whatever sign caused this exception
            returnValue = ""
        }

        return returnValue
    }

    private fun isInRange(value: Double): Boolean {
        return if (max > min) value in min..max else value in max..min
    }
}