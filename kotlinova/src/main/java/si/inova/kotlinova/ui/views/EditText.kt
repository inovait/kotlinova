package si.inova.kotlinova.ui.views

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Created 30. 05. 2018
 *
 * @author Jan Grah
 */

inline fun EditText.addTextAfterChangedListener(crossinline afterTextChangedListener: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChangedListener(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}