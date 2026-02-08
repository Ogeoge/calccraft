package com.calccraft.state

import androidx.lifecycle.ViewModel
import com.calccraft.domain.CalculatorEngine
import com.calccraft.model.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.StringBuilder

class CalcViewModel : ViewModel() {

    private val engine = CalculatorEngine()
    private var historyIdCounter = 0L
    private var isResultOnDisplay = false

    private val _state = MutableStateFlow(CalcState())
    val state: StateFlow<CalcState> = _state.asStateFlow()

    fun onIntent(intent: CalcIntent) {
        when (intent) {
            is CalcIntent.AppendChar -> handleAppend(intent.char)
            is CalcIntent.AppendFunction -> handleAppendFunction(intent.function)
            CalcIntent.Delete -> handleDelete()
            CalcIntent.Clear -> handleClear()
            CalcIntent.Evaluate -> handleEvaluate()
            CalcIntent.ClearHistory -> handleClearHistory()
        }
    }

    private fun handleAppend(char: String) {
        // If a result is on display and the user types a number/dot, start a new expression.
        // If they type an operator, it appends to the result for chained calculations.
        val isStartingNewNumber = isResultOnDisplay && (char.first().isDigit() || char == ".")

        _state.update { currentState ->
            val expressionBeforeUpdate = if (currentState.lastError != null || isStartingNewNumber) {
                ""
            } else {
                currentState.currentExpression
            }
            val newExpression = expressionBeforeUpdate + char

            currentState.copy(
                currentExpression = newExpression,
                displayValue = newExpression.ifEmpty { "0" },
                lastError = null
            )
        }
        isResultOnDisplay = false
    }

    private fun handleAppendFunction(function: String) {
        handleAppend("$function(")
    }

    private fun handleDelete() {
        _state.update { currentState ->
            // If there's an error on display, one backspace clears it and the expression
            if (currentState.lastError != null) {
                return@update CalcState()
            }
            if (currentState.currentExpression.isNotEmpty()) {
                val newExpression = currentState.currentExpression.dropLast(1)
                currentState.copy(
                    currentExpression = newExpression,
                    displayValue = newExpression.ifEmpty { "0" }
                )
            } else {
                currentState
            }
        }
        isResultOnDisplay = false
    }

    private fun handleClear() {
        _state.update { CalcState() }
        isResultOnDisplay = false
    }

    private fun handleEvaluate() {
        val expression = _state.value.currentExpression
        if (expression.isBlank()) return

        val result = engine.evaluate(expression)
        val evaluationTime = System.currentTimeMillis()
        isResultOnDisplay = result.error == null

        _state.update { currentState ->
            val newEntry: HistoryEntry
            val nextState: CalcState

            if (result.error != null) {
                newEntry = HistoryEntry(
                    id = historyIdCounter++,
                    expression = expression,
                    resultText = result.error.message,
                    isError = true,
                    timestampMs = evaluationTime
                )
                nextState = currentState.copy(
                    displayValue = result.error.message,
                    lastError = result.error
                )
            } else if (result.formatted != null) {
                newEntry = HistoryEntry(
                    id = historyIdCounter++,
                    expression = expression,
                    resultText = result.formatted,
                    isError = false,
                    timestampMs = evaluationTime
                )
                nextState = currentState.copy(
                    currentExpression = result.formatted,
                    displayValue = result.formatted,
                    lastError = null
                )
            } else {
                // This case should not be reached based on the EvaluationResult contract
                return@update currentState
            }

            // Atomically update both state and history by adding the new entry to the front
            nextState.copy(history = listOf(newEntry) + currentState.history)
        }
    }

    private fun handleClearHistory() {
        _state.update { it.copy(history = emptyList()) }
    }

    fun getHistoryAsText(): String {
        val history = _state.value.history
        if (history.isEmpty()) {
            return "No history yet."
        }

        val builder = StringBuilder()
        builder.appendLine("CalcCraft History")
        builder.appendLine("-----------------")
        // Iterate in reverse to get chronological order (since we prepend to the list)
        for (entry in history.reversed()) {
            builder.append(entry.expression)
                .append(" = ")
                .append(entry.resultText)
                .appendLine()
        }
        return builder.toString()
    }
}
