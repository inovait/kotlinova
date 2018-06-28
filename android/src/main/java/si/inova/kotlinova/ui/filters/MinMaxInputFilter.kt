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