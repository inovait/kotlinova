package si.inova.kotlinova.compose.result

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ResultKey<T>(val key: Int) : Parcelable
