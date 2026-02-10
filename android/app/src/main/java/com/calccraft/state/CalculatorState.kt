package com.calccraft.state

import com.calccraft.domain.model.HistoryEntry

/**
 * Represents the single source of truth for the calculator's UI state.
 * This corresponds to the `CalculatorState` data model in the contract.
 *
 * @property currentExpression The current input buffer shown in the display.
 * @property lastResult The last successful formatted result, shown in the display's secondary line.
 * @property errorMessage A user-friendly error message shown when an evaluation fails.
 * @property history The in-memory list of past evaluation attempts (most recent first).
 * @property destination The current screen being displayed, either "calculator" or "history".
 */
data class CalculatorState(
    val currentExpression: String = "",
    val lastResult: String? = null,
    val errorMessage: String? = null,
    val history: List<HistoryEntry> = emptyList(),
    val destination: String = "calculator"
)
