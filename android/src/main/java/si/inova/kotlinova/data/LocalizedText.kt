/*
 * Copyright © 2016, Connected Travel, LLC – All Rights Reserved.
 *
 * All information contained herein is property of Connected Travel, LLC including, but
 * not limited to, technical and intellectual concepts which may be embodied within.
 *
 * Dissemination or reproduction of this material is strictly forbidden unless prior written
 * permission, via license, is obtained from Connected Travel, LLC. If permission is obtained,
 * this notice, and any other such legal notices, must remain unaltered.
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