package com.calccraft.state

import androidx.lifecycle.ViewModel
import com.calccraft.domain.engine.ExpressionEngine
import com.calccraft.domain.model.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Bridges the UI layer with the domain and state layers.
 * - Holds the application state in a [StateFlow].
 * - Processes UI actions ([CalculatorIntent]) and orchestrates state updates.
 * - Coordinates with the [ExpressionEngine] for calculations and the [CalculatorReducer]
 *   for pure state transformations.
 */
class CalculatorViewModel : ViewModel() {

    private val reducer = CalculatorReducer
    private val expressionEngine = ExpressionEngine()

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    /**
     * The single entry point for the UI to dispatch actions.
     */
    fun onIntent(intent: CalculatorIntent) {
        _state.update { currentState ->
            reducer.reduce(currentState, intent, expressionEngine::evaluate)
        }
    }

    /**
     * Handles the evaluation of the current expression.
     * This involves a side effect (calling the expression engine), so it's managed
     * by the ViewModel.
     */

}
