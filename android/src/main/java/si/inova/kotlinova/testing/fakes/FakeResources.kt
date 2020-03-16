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

package si.inova.kotlinova.testing.fakes

import android.annotation.SuppressLint
import android.content.res.*
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.StringRes
import java.io.InputStream

@Suppress("DEPRECATION")
@SuppressLint("UseSparseArrays")
class FakeResources : Resources(null, null, null) {
    private val stringMap = HashMap<Int, (Array<out Any?>) -> String>()
    private var lastStringArguments: List<Any>? = null

    var useResourceIdAsPlaceholder: Boolean = false

    fun putString(@StringRes resource: Int, text: String) {
        putString(resource) { text }
    }

    fun putString(@StringRes resource: Int, textGenerator: (Array<out Any?>) -> String) {
        stringMap[resource] = textGenerator
    }

    override fun getString(id: Int): String {
        val generator = getStringOrFallback(id)

        return generator(emptyArray())
    }

    private fun getStringOrFallback(id: Int): (Array<out Any?>) -> String {
        val generator = stringMap[id]

        if (generator == null) {
            if (useResourceIdAsPlaceholder) {
                return { id.toString() }
            } else {
                error("String $id not faked")
            }
        }

        return generator
    }

    override fun getString(id: Int, vararg formatArgs: Any?): String {
        val generator = getStringOrFallback(id)

        return generator(formatArgs)
    }

    override fun getTextArray(id: Int): Array<CharSequence> {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun obtainTypedArray(id: Int): TypedArray {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getAnimation(id: Int): XmlResourceParser {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getText(id: Int): CharSequence {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getText(id: Int, def: CharSequence?): CharSequence {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getDisplayMetrics(): DisplayMetrics {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getDrawableForDensity(id: Int, density: Int): Drawable? {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getConfiguration(): Configuration {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun obtainAttributes(set: AttributeSet?, attrs: IntArray?): TypedArray {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getDimensionPixelSize(id: Int): Int {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getIntArray(id: Int): IntArray {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getValue(id: Int, outValue: TypedValue?, resolveRefs: Boolean) {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getValue(name: String?, outValue: TypedValue?, resolveRefs: Boolean) {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any?): String {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getQuantityString(id: Int, quantity: Int): String {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getResourcePackageName(resid: Int): String {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getStringArray(id: Int): Array<String> {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun openRawResourceFd(id: Int): AssetFileDescriptor {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getDimension(id: Int): Float {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getColorStateList(id: Int): ColorStateList {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getColorStateList(id: Int, theme: Theme?): ColorStateList {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getBoolean(id: Int): Boolean {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getQuantityText(id: Int, quantity: Int): CharSequence {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getColor(id: Int): Int {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getColor(id: Int, theme: Theme?): Int {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun openRawResource(id: Int): InputStream {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun openRawResource(id: Int, value: TypedValue?): InputStream {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getMovie(id: Int): android.graphics.Movie {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getInteger(id: Int): Int {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun parseBundleExtras(parser: XmlResourceParser?, outBundle: Bundle?) {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getDrawable(id: Int): Drawable {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getDrawable(id: Int, theme: Theme?): Drawable {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getResourceTypeName(resid: Int): String {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getLayout(id: Int): XmlResourceParser {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getFont(id: Int): Typeface {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun updateConfiguration(config: Configuration?, metrics: DisplayMetrics?) {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getXml(id: Int): XmlResourceParser {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getResourceName(resid: Int): String {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun parseBundleExtra(tagName: String?, attrs: AttributeSet?, outBundle: Bundle?) {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getDimensionPixelOffset(id: Int): Int {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getValueForDensity(
        id: Int,
        density: Int,
        outValue: TypedValue?,
        resolveRefs: Boolean
    ) {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getResourceEntryName(resid: Int): String {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }

    override fun getFraction(id: Int, base: Int, pbase: Int): Float {
        throw UnsupportedOperationException("Method not supported by FakeResources")
    }
}