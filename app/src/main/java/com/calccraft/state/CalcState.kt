package com.calccraft.state

import com.calccraft.domain.CalcError
import com.calccraft.model.HistoryEntry

/**
 * Single source of truth for Compose UI state in CalcViewModel.
 *
 * @property currentExpression Current expression being edited.
 * @property displayValue What is currently shown in the calculator display (expression, result, or error message).
 * @property lastError Last evaluation error (if any).
 * @property history In-memory list of prior evaluations.
 */
data class CalcState(
    val currentExpression: String = "",
    val displayValue: String = "0",
    val lastError: CalcError? = null,
    val history: List<HistoryEntry> = emptyList()
)
