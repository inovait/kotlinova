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