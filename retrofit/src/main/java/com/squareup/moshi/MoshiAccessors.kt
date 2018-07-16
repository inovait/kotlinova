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