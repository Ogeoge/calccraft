package com.calccraft.domain.engine

import com.calccraft.domain.model.EvalResult

/**
 * A placeholder for the expression evaluation engine.
 * The full implementation will use a tokenizer, shunting-yard algorithm,
 * and a Reverse Polish Notation (RPN) evaluator to safely compute results.
 */
class ExpressionEngine {
    /**
     * Evaluates a mathematical expression string.
     * This is a dummy implementation to allow the application to compile.
     * @param expression The mathematical expression to evaluate.
     * @return An [EvalResult] which is either a [EvalResult.Success] or [EvalResult.Error].
     */
    fun evaluate(expression: String): EvalResult {
        // A real implementation would delegate to Tokenizer, ShuntingYard, and RpnEvaluator.
        return if (expression.contains("/ 0")) {
            EvalResult.Error("DivideByZero", "Cannot divide by zero.")
        } else if (expression.isNotBlank()) {
            // This is not a real evaluation, just a placeholder.
            EvalResult.Success(0.0, "0")
        } else {
            EvalResult.Error("InvalidSyntax", "Expression is empty.")
        }
    }
}
