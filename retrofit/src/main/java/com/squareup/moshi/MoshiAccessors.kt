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

package com.squareup.moshi

import com.squareup.moshi.Moshi.BUILT_IN_FACTORIES

/**
 * Remove default [ClassJsonAdapter] from Moshi.
 *
 * [ClassJsonAdapter] can cause hidden issues if developer is primarily using Kotlin files
 * and forgets to add *@JsonClass(generateAdapter = true)* annotation to the class.
 *
 * This method removes the adapter from [Moshi]. Must be called before first [Moshi] instance is
 * created.
 */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    " This safeguard accesses non-public Moshi API and can break any time. " +
        "Do not use this class from Kotlinova. Instead copy paste this file into " +
        "your project (sans deprecation warning) and use it directly. " +
        "That way you get compile error if this breaks."
)
fun removeMoshiClassJsonAdapter() {
    BUILT_IN_FACTORIES.remove(ClassJsonAdapter.FACTORY)
}