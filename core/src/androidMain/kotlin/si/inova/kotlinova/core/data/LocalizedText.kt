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

package si.inova.kotlinova.core.data

import android.content.Context
import android.content.res.Resources
import android.os.Parcelable
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * System that allows passing localized text from model to view without the need to attach context.
 */
interface LocalizedText : Parcelable {
   fun get(resources: Resources): CharSequence

   override fun equals(other: Any?): Boolean
   override fun hashCode(): Int
}

@Parcelize
object EmptyText : LocalizedText {
   override fun get(resources: Resources): CharSequence {
      return ""
   }

   override fun equals(other: Any?): Boolean {
      return other is EmptyText
   }

   override fun hashCode(): Int {
      return "EmptyText".hashCode()
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
   @StringRes
   private val resource: Int,
   private vararg val arguments: @RawValue Any,
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

   override fun toString(): String {
      return "ResourceText(resource=$resource, arguments=${arguments.contentToString()})"
   }
}

@Parcelize
class PluralText(
   @PluralsRes
   private val resource: Int,
   private val quantity: Int,
   private vararg val arguments: @RawValue Any,
) : LocalizedText {
   override fun get(resources: Resources): CharSequence {
      return resources.getQuantityString(resource, quantity, *arguments)
   }

   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is PluralText) return false

      if (resource != other.resource) return false
      if (quantity != other.quantity) return false
      if (!arguments.contentEquals(other.arguments)) return false

      return true
   }

   override fun hashCode(): Int {
      var result = resource
      result = 31 * result + quantity
      result = 31 * result + arguments.contentHashCode()
      return result
   }

   override fun toString(): String {
      return "PluralText(resource=$resource, quantity=$quantity, arguments=${arguments.contentToString()})"
   }
}

fun LocalizedText.get(context: Context) = get(context.resources)
