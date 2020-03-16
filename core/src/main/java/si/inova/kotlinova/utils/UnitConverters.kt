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

@file:JvmName("UnitConverters")

package si.inova.kotlinova.utils

import kotlin.math.floor

/**
 * @author Matej Drobnic
 */
object Centimeters {
    @JvmStatic
    fun toFeetInches(centimeters: Double): FeetInches {
        val inches = centimeters * 0.393701

        val feet = floor(inches / 12)

        return FeetInches(feet, inches % 12)
    }

    @JvmStatic
    fun toInches(centimeters: Double): Double {
        return centimeters * 0.393701
    }
}

object Kilograms {
    @JvmStatic
    fun toPounds(kilograms: Double): Double {
        return kilograms * 0002.20462
    }
}

object Pounds {
    @JvmStatic
    fun toKilograms(pounds: Double): Double {
        return pounds / 2.20462
    }
}

data class FeetInches(val feet: Double, val inches: Double) {
    fun toCentimeters(): Double {
        return feet * 30.48 + inches * 2.54
    }
}

object Meters {
    @JvmStatic
    fun toMiles(meters: Double): Double {
        return meters * 0.000621371
    }

    @JvmStatic
    fun toKilometers(meters: Double): Double {
        return meters / 1000.0
    }
}

object Inches {
    @JvmStatic
    fun toCentimeters(inches: Double): Double {
        return inches / 0.393701
    }
}