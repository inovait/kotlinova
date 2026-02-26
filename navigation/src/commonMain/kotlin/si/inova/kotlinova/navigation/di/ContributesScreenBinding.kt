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

package si.inova.kotlinova.navigation.di

import kotlin.reflect.KClass

/**
 * Bind this screen to its parent. This annotation can be used to have one screen depend on another screen without having to
 * have hard reference to its implementation.
 *
 * Instead, a screen can depend on an abstract screen, with its implementation being created somewhere else and bound to the
 * abstract screen with this annotation.
 *
 * For example:
 *
 * ```kotlin
 * abstract class InnerScreen : Screen<ScreenKey>
 *
 * class OuterScreen(
 *    private val innerSubscreen: InnerScreen,
 *    ) : Screen<OuterScreenKey>() {
 *       ...
 *    }
 *
 * @ContributesScreenBinding
 * class InnerScreenImpl : InnerScreen {
 *    @Composable
 *    override fun Content(key: ScreenKey) {
 *       ...
 *    }
 * }
 */
annotation class ContributesScreenBinding(
   /**
    * The type of the parent screen that this screen is bound to. If not specified, direct parent screen concrete class is used.
    */
   val boundType: KClass<*> = Unit::class,
)
