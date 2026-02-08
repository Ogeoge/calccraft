package com.calccraft.model

data class HistoryEntry(
    val id: Long,
    val expression: String,
    val resultText: String,
    val isError: Boolean,
    val timestampMs: Long,
)
