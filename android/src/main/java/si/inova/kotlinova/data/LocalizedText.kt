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

package si.inova.kotlinova.data

import android.content.Context
import android.content.res.Resources
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

/**
 * System that allows passing localized text from model to view without the need to attach context.
 *
 * All implementors of this interface MUST properly implement equals and hashcode
 */
interface LocalizedText : Parcelable {
    fun get(resources: Resources): CharSequence
}

@Parcelize
object EmptyText : LocalizedText {
    override fun get(resources: Resources): CharSequence {
        return ""
    }
}

@Parcelize
data class LiteralText(private val text: CharSequence) : LocalizedText {
    override fun get(resources: Resources): CharSequence {
        return text
    }
}

@Parcelize
class ResourceText(
    @StringRes private val resource: Int,
    private vararg val arguments: @RawValue Any
) : LocalizedText {
    override fun get(resources: Resources): CharSequence {
        return resources.getString(resource, *arguments)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourceText

        if (resource != other.resource) return false
        if (!arguments.contentEquals(other.arguments)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = resource
        result = 31 * result + arguments.contentHashCode()
        return result
    }
}

fun LocalizedText.get(context: Context) = get(context.resources)