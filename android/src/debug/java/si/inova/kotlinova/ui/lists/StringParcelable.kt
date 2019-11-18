package si.inova.kotlinova.ui.lists

import android.os.Parcel
import android.os.Parcelable

class StringParcelable(val text: String) : Parcelable {
    constructor(parcel: Parcel) :
        this(parcel.readString() ?: error("System returned null string"))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StringParcelable> {
        override fun createFromParcel(parcel: Parcel): StringParcelable {
            return StringParcelable(parcel)
        }

        override fun newArray(size: Int): Array<StringParcelable?> {
            return arrayOfNulls(size)
        }
    }
}