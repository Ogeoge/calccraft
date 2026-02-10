package com.calccraft.state

import com.calccraft.domain.model.EvalResult
import com.calccraft.domain.model.HistoryEntry
import java.util.UUID

/**
 * A pure function that takes the current [CalculatorState] and a [CalculatorIntent],
 * and returns a new [CalculatorState]. This encapsulates the core business logic
 * of the calculator in a testable and predictable way.
 */
object CalculatorReducer {

    /**
     * Reduces the current state with a given intent to produce a new state.
     *
     * @param currentState The current state of the calculator.
     * @param intent The user-initiated action.
     * @param evaluate A lambda function that takes an expression string and returns an [EvalResult].
     *                 This dependency is injected to keep the reducer pure.
     * @return The new [CalculatorState].
     */
    fun reduce(
        currentState: CalculatorState,
        intent: CalculatorIntent,
        evaluate: (String) -> EvalResult
    ): CalculatorState {
        return when (intent) {
            is CalculatorIntent.Append -> handleAppend(currentState, intent.text)
            is CalculatorIntent.Delete -> handleDelete(currentState)
            is CalculatorIntent.Clear -> handleClear(currentState)
            is CalculatorIntent.Evaluate -> handleEvaluate(currentState, evaluate)
            is CalculatorIntent.ClearHistory -> currentState.copy(history = emptyList())
            is CalculatorIntent.SwitchDestination -> handleSwitchDestination(currentState, intent.destination)
            is CalculatorIntent.UseHistoryEntry -> handleUseHistoryEntry(currentState, intent.historyEntryId)
        }
    }

    private fun handleAppend(state: CalculatorState, text: String?): CalculatorState {
        if (text.isNullOrEmpty()) return state

        // Prevent multiple decimals in one number segment
        if (text == ".") {
            val lastSegment = state.currentExpression.split(Regex("[+\\-*/()]")).last()
            if ("." in lastSegment) return state
        }

        val isResultOnDisplay = state.lastResult == "= ${state.currentExpression}"
        val isOperator = text in "+-*/%"

        val shouldStartNew = (isResultOnDisplay && !isOperator) || state.errorMessage != null

        var currentExpression = if (shouldStartNew) "" else state.currentExpression

        if (currentExpression == "0" && text != ".") {
            currentExpression = ""
        }

        var textToAppend = text
        val lastChar = currentExpression.trim().lastOrNull()
        if (text == "(" && (lastChar?.isDigit() == true || lastChar == ')')) {
            textToAppend = "*("
        }

        return state.copy(
            currentExpression = currentExpression + textToAppend,
            errorMessage = null
        )
    }

    private fun handleDelete(state: CalculatorState): CalculatorState {
        if (state.currentExpression.isEmpty()) {
            return state
        }
        return state.copy(
            currentExpression = state.currentExpression.dropLast(1),
            errorMessage = null // Clear error when user corrects input
        )
    }

    private fun handleClear(state: CalculatorState): CalculatorState {
        return state.copy(
            currentExpression = "",
            errorMessage = null
            // Keep lastResult visible until next evaluation
        )
    }

    private fun handleEvaluate(state: CalculatorState, evaluate: (String) -> EvalResult): CalculatorState {
        val expression = state.currentExpression.trim()
        if (expression.isEmpty()) {
            return state
        }

        val result = evaluate(expression)
        val historyEntry = HistoryEntry(
            id = UUID.randomUUID().toString(),
            timestampMs = System.currentTimeMillis(),
            expression = expression,
            result = result
        )

        val newHistory = listOf(historyEntry) + state.history

        return when (result) {
            is EvalResult.Success -> {
                state.copy(
                    currentExpression = result.formatted,
                    lastResult = "= ${result.formatted}",
                    errorMessage = null,
                    history = newHistory
                )
            }
            is EvalResult.Error -> {
                state.copy(
                    // Do not clear expression on error, so user can fix it
                    errorMessage = result.message,
                    history = newHistory
                )
            }
        }
    }

    private fun handleSwitchDestination(state: CalculatorState, destination: String?): CalculatorState {
        return if (destination == "calculator" || destination == "history") {
            state.copy(destination = destination)
        } else {
            state // Ignore invalid destinations
        }
    }

    private fun handleUseHistoryEntry(state: CalculatorState, entryId: String?): CalculatorState {
        val entry = state.history.find { it.id == entryId }
        return if (entry != null) {
            state.copy(
                currentExpression = entry.expression,
                destination = "calculator",
                errorMessage = null, // Clear any current error
                lastResult = if(entry.result is EvalResult.Success) "= ${entry.result.formatted}" else null
            )
        } else {
            state
        }
    }
}
