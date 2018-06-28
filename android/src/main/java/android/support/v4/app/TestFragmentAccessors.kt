/**
 * @author Matej Drobnic
 */

package android.support.v4.app

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

fun Fragment.setHost(host: FragmentHostCallback<*>) {
    this.mHost = host
}

fun Fragment.setContext(context: Context) {
    setHost(mock<FragmentHostCallback<Unit>>()
        .apply { whenever(getContext()).thenReturn(context) })
        .apply {
            if (context is FragmentActivity) {
                whenever(activity).thenReturn(context)
            }
        }
}
