package com.calccraft.domain

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object RpnEvaluator {

    fun evaluate(rpn: List<Token>): EvaluationResult {
        if (rpn.isEmpty()) {
            return EvaluationResult(
                value = null,
                formatted = null,
                error = CalcError(
                    code = CalcErrorCode.EMPTY_EXPRESSION,
                    message = "Empty expression",
                    position = null
                )
            )
        }

        val stack = ArrayDeque<Double>()

        fun popValue(positionHint: Int? = null): Double? {
            return if (stack.isEmpty()) {
                null
            } else {
                stack.removeLast()
            }
        }

        for (t in rpn) {
            when (t) {
                is Token.Number -> stack.addLast(t.value)

                is Token.Operator -> {
                    when (t.op) {
                        Operator.PLUS,
                        Operator.MINUS,
                        Operator.TIMES,
                        Operator.DIV,
                        Operator.POW -> {
                            val b = popValue(t.position)
                            val a = popValue(t.position)
                            if (a == null || b == null) {
                                return invalidSyntax("Invalid syntax", t.position)
                            }

                            val res = when (t.op) {
                                Operator.PLUS -> a + b
                                Operator.MINUS -> a - b
                                Operator.TIMES -> a * b
                                Operator.DIV -> {
                                    if (b == 0.0) {
                                        return EvaluationResult(
                                            value = null,
                                            formatted = null,
                                            error = CalcError(
                                                code = CalcErrorCode.DIVIDE_BY_ZERO,
                                                message = "Division by zero",
                                                position = t.position
                                            )
                                        )
                                    }
                                    a / b
                                }
                                Operator.POW -> a.pow(b)
                            }

                            if (!res.isFinite()) {
                                return domainError("Result out of range", t.position)
                            }
                            stack.addLast(res)
                        }

                        Operator.NEGATE -> {
                            val a = popValue(t.position)
                            if (a == null) return invalidSyntax("Invalid syntax", t.position)
                            val res = -a
                            if (!res.isFinite()) return domainError("Result out of range", t.position)
                            stack.addLast(res)
                        }

                        Operator.PERCENT -> {
                            val a = popValue(t.position)
                            if (a == null) return invalidSyntax("Invalid syntax", t.position)
                            val res = a / 100.0
                            if (!res.isFinite()) return domainError("Result out of range", t.position)
                            stack.addLast(res)
                        }
                    }
                }

                is Token.Function -> {
                    val a = popValue(t.position)
                    if (a == null) return invalidSyntax("Invalid syntax", t.position)

                    val res = when (t.name.lowercase()) {
                        "sqrt", "√" -> {
                            if (a < 0.0) {
                                return domainError("sqrt() domain error", t.position)
                            }
                            sqrt(a)
                        }

                        "sin" -> sin(a)
                        "cos" -> cos(a)
                        "tan" -> tan(a)

                        "log" -> {
                            // Natural log by design; engine can document that log() is ln().
                            if (a <= 0.0) {
                                return domainError("log() domain error", t.position)
                            }
                            ln(a)
                        }

                        else -> {
                            return EvaluationResult(
                                value = null,
                                formatted = null,
                                error = CalcError(
                                    code = CalcErrorCode.UNKNOWN_TOKEN,
                                    message = "Unknown function: ${t.name}",
                                    position = t.position
                                )
                            )
                        }
                    }

                    if (!res.isFinite()) {
                        return domainError("Result out of range", t.position)
                    }
                    stack.addLast(res)
                }

                is Token.LeftParen,
                is Token.RightParen,
                is Token.Comma -> {
                    // Parentheses and commas should not appear in RPN output; treat as syntax error.
                    return invalidSyntax("Invalid syntax", t.position)
                }
            }
        }

        if (stack.size != 1) {
            return invalidSyntax("Invalid syntax", null)
        }

        val value = stack.removeLast()
        if (!value.isFinite()) {
            return domainError("Result out of range", null)
        }

        return EvaluationResult(value = value, formatted = null, error = null)
    }

    private fun invalidSyntax(message: String, position: Int?): EvaluationResult {
        return EvaluationResult(
            value = null,
            formatted = null,
            error = CalcError(
                code = CalcErrorCode.INVALID_SYNTAX,
                message = message,
                position = position
            )
        )
    }

    private fun domainError(message: String, position: Int?): EvaluationResult {
        return EvaluationResult(
            value = null,
            formatted = null,
            error = CalcError(
                code = CalcErrorCode.DOMAIN_ERROR,
                message = message,
                position = position
            )
        )
    }
}
