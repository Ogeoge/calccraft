package com.calccraft.state

/**
 * Represents user actions or events that can modify the calculator's state.
 * This corresponds to the `CalculatorIntent` data model in the contract.
 * These intents are processed by the `CalculatorReducer` to produce a new `CalculatorState`.
 */
sealed class CalculatorIntent {

    /**
     * Appends a character or string to the current expression.
     * Corresponds to `type = 'Append'`.
     * @param text The text to append (e.g., a digit, operator, or function name).
     */
    data class Append(val text: String) : CalculatorIntent()

    /**
     * Deletes the last character from the current expression.
     * Corresponds to `type = 'Delete'`.
     */
    object Delete : CalculatorIntent()

    /**
     * Clears the entire current expression, last result, and any error message.
     * Corresponds to `type = 'Clear'`.
     */
    object Clear : CalculatorIntent()

    /**
     * Triggers the evaluation of the current expression.
     * Corresponds to `type = 'Evaluate'`.
     */
    object Evaluate : CalculatorIntent()

    /**
     * Clears the entire calculation history.
     * Corresponds to `type = 'ClearHistory'`.
     */
    object ClearHistory : CalculatorIntent()

    /**
     * Switches the currently displayed screen (destination).
     * Corresponds to `type = 'SwitchDestination'`.
     * @param destination The name of the destination to switch to (e.g., "calculator", "history").
     */
    data class SwitchDestination(val destination: String) : CalculatorIntent()

    /**
     * Loads an expression from a specific history entry into the current input.
     * Corresponds to `type = 'UseHistoryEntry'`.
     * @param historyEntryId The unique ID of the history entry to use.
     */
    data class UseHistoryEntry(val historyEntryId: String) : CalculatorIntent()
}
