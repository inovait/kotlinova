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

package si.inova.kotlinova.navigation.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

internal val ACTIVITY_SCOPE_ANNOTATION = ClassName(
   "si.inova.kotlinova.navigation.di",
   "BackstackScope"
)

internal val FROM_BACKSTACK_QUALIFIER_ANNOTATION = ClassName(
   "si.inova.kotlinova.navigation.di",
   "FromBackstack"
)

internal val SCREEN_BASE_CLASS = ClassName(
   "si.inova.kotlinova.navigation.screens",
   "Screen"
)

internal val SCREEN_KEY_BASE_CLASS = ClassName(
   "si.inova.kotlinova.navigation.screenkeys",
   "ScreenKey"
)

internal val SCOPED_SERVICE_BASE_CLASS = ClassName(
   "si.inova.kotlinova.navigation.services",
   "ScopedService"
)

internal val SIMPLE_STACK_BACKSTACK_CLASS = ClassName(
   "com.zhuinden.simplestack",
   "Backstack"
)

internal val APPLICATION_SCOPE_ANNOTATION = ClassName(
   "si.inova.kotlinova.navigation.di",
   "OuterNavigationScope"
)

internal val SCREEN_REGISTRATION = ClassName(
   "si.inova.kotlinova.navigation.di",
   "ScreenRegistration"
)

internal val SCREEN_FACTORY = ClassName(
   "si.inova.kotlinova.navigation.di",
   "ScreenFactory"
)

internal val LOOKUP_FROM_SCOPE_WITH_INHERITANCE = MemberName(
   "si.inova.kotlinova.navigation.simplestack",
   "lookupFromScopeWithParentFallback"
)

internal val ANNOTATION_CURRENT_SCOPE_TAG = ClassName(
   "si.inova.kotlinova.navigation.services",
   "CurrentScopeTag"
)

internal val ANNOTATION_INHERITED = ClassName(
   "si.inova.kotlinova.navigation.services",
   "Inherited"
)
