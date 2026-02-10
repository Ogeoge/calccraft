package com.calccraft.domain.model

/**
 * Represents an in-memory history item for one evaluation attempt.
 * This corresponds to the `HistoryEntry` data model in the contract.
 *
 * @property id A unique identifier for the entry (e.g., a UUID string).
 * @property timestampMs The epoch milliseconds when the evaluation occurred.
 * @property expression The user-entered expression string that was evaluated.
 * @property result The outcome of the evaluation, which can be a [EvalResult.Success] or [EvalResult.Error].
 */
data class HistoryEntry(
    val id: String,
    val timestampMs: Long,
    val expression: String,
    val result: EvalResult
)
