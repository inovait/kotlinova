/*
 * Copyright 2026 INOVA IT d.o.o.
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

package si.inova.kotlinova.navigation.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

/**
 *  A [kotlinx.serialization.KSerializer] for handling polymorphic class hierarchies on JVM.
 *
 *  It uses reflection under the hood, so it will not work on the other platforms
 *  (they will still need reflection hierarchies defined manually in the SerializersModule).
 */
@OptIn(InternalSerializationApi::class)
class ReflectivePolymorphicSerializer<T : Any>(baseClass: Class<T>) : KSerializer<T> {
   override val descriptor: SerialDescriptor = buildClassSerialDescriptor(serialName = "Polymorphic<${baseClass.name}>") {
      element(elementName = "type", descriptor = serialDescriptor<String>())
      element(
         elementName = "value",
         descriptor = buildClassSerialDescriptor(serialName = "Any"),
      )
   }

   override fun serialize(encoder: Encoder, value: T) {
      encoder.encodeStructure(descriptor) {
         val className = value::class.java.name
         encodeStringElement(descriptor, index = 0, className)
         @Suppress("UNCHECKED_CAST")
         val serializer = value::class.serializer() as KSerializer<T>
         encodeSerializableElement(descriptor, index = 1, serializer, value)
      }
   }

   override fun deserialize(decoder: Decoder): T {
      return decoder.decodeStructure(descriptor) {
         val className = decodeStringElement(descriptor, decodeElementIndex(descriptor))
         val serializer = Class.forName(className).kotlin.serializer()
         @Suppress("UNCHECKED_CAST")
         decodeSerializableElement(descriptor, decodeElementIndex(descriptor), serializer) as T
      }
   }
}
