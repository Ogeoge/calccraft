package com.calccraft.state

/**
 * Represents user actions (intents) that can be dispatched from the UI
 * to the CalcViewModel to modify the calculator's state.
 */
sealed interface CalcIntent {
    /**
     * Appends a string to the current expression.
     * Used for digits, operators, and functions (e.g., "sin(").
     * @param value The string to append.
     */
    data class Input(val value: String) : CalcIntent

    /**
     * Deletes the last character from the current expression.
     */
    object Delete : CalcIntent

    /**
     * Clears the entire expression and any displayed result or error.
     */
    object Clear : CalcIntent

    /**
     * Evaluates the current expression.
     */
    object Evaluate : CalcIntent
}
