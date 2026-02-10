package com.calccraft.domain.engine

import java.util.ArrayDeque
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

// Custom exceptions for evaluation errors, to be caught by the ExpressionEngine facade.
sealed class EvaluationException(message: String) : RuntimeException(message)
class DivideByZeroException : EvaluationException("Division by zero")
class DomainErrorException(message: String) : EvaluationException(message)
class StackUnderflowException(message: String) : EvaluationException(message)
class InvalidExpressionException(message: String) : EvaluationException(message)

/**
 * Evaluates a list of tokens in Reverse Polish Notation (RPN).
 * This class is the final step in the expression evaluation pipeline, taking output from [ShuntingYard].
 */
object RpnEvaluator {

    /**
     * Evaluates the given RPN token list to a single numeric result.
     *
     * @param rpnTokens The list of tokens in RPN order.
     * @return The final calculated value as a [Double].
     * @throws EvaluationException if any error occurs during evaluation (e.g., division by zero, invalid domain).
     */
    fun evaluate(rpnTokens: List<Token>): Double {
        val stack = ArrayDeque<Double>()

        for (token in rpnTokens) {
            when (token) {
                is Token.Number -> stack.push(token.value)
                is Token.Constant -> stack.push(resolveConstant(token))
                is Token.Operator -> applyOperator(token, stack)
                is Token.Function -> applyFunction(token, stack)
                // Parentheses and commas should not appear in the RPN queue.
                else -> throw InvalidExpressionException("Unexpected token in RPN queue: $token")
            }
        }

        if (stack.size != 1) {
            throw InvalidExpressionException("Evaluation stack did not result in a single value. Size: ${stack.size}")
        }

        val result = stack.pop()
        if (!result.isFinite()) {
            throw DomainErrorException("Result is not a finite number (e.g., infinity from tan(pi/2))")
        }
        return result
    }

    private fun resolveConstant(token: Token.Constant): Double {
        return when (token.name) {
            "pi" -> PI
            "e" -> Math.E
            else -> throw InvalidExpressionException("Unknown constant: ${token.name}")
        }
    }

    private fun applyOperator(operator: Token.Operator, stack: ArrayDeque<Double>) {
        when (operator) {
            Token.Operator.Add -> {
                val b = popOperand(stack)
                val a = popOperand(stack)
                stack.push(a + b)
            }
            Token.Operator.Subtract -> {
                val b = popOperand(stack)
                val a = popOperand(stack)
                stack.push(a - b)
            }
            Token.Operator.Multiply -> {
                val b = popOperand(stack)
                val a = popOperand(stack)
                stack.push(a * b)
            }
            Token.Operator.Divide -> {
                val b = popOperand(stack)
                val a = popOperand(stack)
                if (b == 0.0) throw DivideByZeroException()
                stack.push(a / b)
            }
            Token.Operator.UnaryMinus -> {
                val a = popOperand(stack)
                stack.push(-a)
            }
            Token.Operator.Percent -> {
                val a = popOperand(stack)
                stack.push(a / 100.0)
            }
        }
    }

    private fun applyFunction(func: Token.Function, stack: ArrayDeque<Double>) {
        when (func.name) {
            "sin" -> {
                val a = popOperand(stack)
                stack.push(sin(a))
            }
            "cos" -> {
                val a = popOperand(stack)
                stack.push(cos(a))
            }
            "tan" -> {
                val a = popOperand(stack)
                stack.push(tan(a))
            }
            "log" -> {
                val a = popOperand(stack)
                if (a <= 0) throw DomainErrorException("Logarithm argument must be positive")
                stack.push(ln(a))
            }
            "sqrt" -> {
                val a = popOperand(stack)
                if (a < 0) throw DomainErrorException("Square root argument must be non-negative")
                stack.push(sqrt(a))
            }
            "pow" -> {
                val b = popOperand(stack)
                val a = popOperand(stack)
                stack.push(a.pow(b))
            }
            else -> throw InvalidExpressionException("Unknown function: ${func.name}")
        }
    }

    private fun popOperand(stack: ArrayDeque<Double>): Double {
        if (stack.isEmpty()) {
            throw StackUnderflowException("Not enough operands for operation.")
        }
        return stack.pop()
    }
}
