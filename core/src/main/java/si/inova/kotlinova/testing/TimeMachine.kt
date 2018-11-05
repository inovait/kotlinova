package si.inova.kotlinova.testing

/**
 * Common interface for all time-controlling JUnit Rules
 */
interface TimeMachine {
    /**
     * Current time in [System.currentTimeMillis] format
     */
    val now: Long

    /**
     * Moves the Scheduler's clock forward by a specified amount of milliseconds.
     */
    fun advanceTime(ms: Long)
}