package com.calccraft.domain

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * High-level offline calculator API.
 *
 * Pipeline:
 *  - Tokenizer: String -> List<Token>
 *  - ShuntingYard: infix tokens -> RPN tokens
 *  - RpnEvaluator: RPN -> Double
 *
 * No network calls. No persistence.
 */
class CalculatorEngine(
    private val tokenizer: Tokenizer = Tokenizer(),
    private val shuntingYard: ShuntingYard = ShuntingYard(),
    private val evaluator: RpnEvaluator = RpnEvaluator(::defaultFunctionImpl),
) {

    fun evaluate(expression: String): EvaluationResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) {
            return EvaluationResult(
                error = CalcError(
                    code = CalcError.Code.EMPTY_EXPRESSION,
                    message = "Enter an expression",
                    position = null,
                ),
            )
        }

        return try {
            val tokens = tokenizer.tokenize(trimmed)
            val rpn = shuntingYard.toRpn(tokens)
            val value = evaluator.evaluate(rpn)
            val formatted = format(value)
            EvaluationResult(value = value, formatted = formatted, error = null)
        } catch (e: CalcException) {
            EvaluationResult(value = null, formatted = null, error = e.error)
        } catch (e: ArithmeticException) {
            EvaluationResult(
                error = CalcError(
                    code = CalcError.Code.INVALID_SYNTAX,
                    message = e.message ?: "Invalid syntax",
                    position = null,
                ),
            )
        } catch (t: Throwable) {
            // Fail closed: never crash UI; surface a safe message.
            EvaluationResult(
                error = CalcError(
                    code = CalcError.Code.INVALID_SYNTAX,
                    message = "Invalid expression",
                    position = null,
                ),
            )
        }
    }

    fun format(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return value.toString()

        // Render as a trimmed decimal string; avoid scientific notation for common values.
        // Keep it simple/minimal: Double.toString() then trim trailing zeros when decimal.
        val raw = value.toString()
        if (!raw.contains('.')) return raw

        var s = raw
        // Handle scientific notation by leaving as-is (still valid display text).
        if (s.contains('E') || s.contains('e')) return s

        while (s.endsWith('0')) s = s.dropLast(1)
        if (s.endsWith('.')) s = s.dropLast(1)
        if (s == "-0") s = "0"
        return s
    }

    fun buildHistoryResultText(result: EvaluationResult): String {
        return when {
            result.error != null -> result.error.message
            result.formatted != null -> result.formatted
            result.value != null -> format(result.value)
            else -> "Invalid expression"
        }
    }

    fun buildExportText(entries: List<com.calccraft.model.HistoryEntry>): String {
        if (entries.isEmpty()) return ""
        return entries
            .sortedBy { it.id }
            .joinToString(separator = "\n") { "${it.expression} = ${it.resultText}" }
    }

    private fun defaultFunctionImpl(name: String, args: List<Double>): Double {
        val n = name.lowercase()
        return when (n) {
            "sqrt", "√" -> {
                val x = args.requireArity(1, name)
                if (x < 0.0) throw CalcException(
                    CalcError(
                        code = CalcError.Code.DOMAIN_ERROR,
                        message = "sqrt domain error",
                        position = null,
                    ),
                )
                sqrt(x)
            }

            "sin" -> sin(args.requireArity(1, name))
            "cos" -> cos(args.requireArity(1, name))
            "tan" -> tan(args.requireArity(1, name))
            "log" -> {
                val x = args.requireArity(1, name)
                if (x <= 0.0) throw CalcException(
                    CalcError(
                        code = CalcError.Code.DOMAIN_ERROR,
                        message = "log domain error",
                        position = null,
                    ),
                )
                // Natural log; UI/contract doesn't specify base; keep consistent.
                ln(x)
            }

            "pow" -> {
                if (args.size != 2) throw CalcException(
                    CalcError(
                        code = CalcError.Code.INVALID_SYNTAX,
                        message = "pow requires 2 arguments",
                        position = null,
                    ),
                )
                args[0].pow(args[1])
            }

            else -> throw CalcException(
                CalcError(
                    code = CalcError.Code.UNKNOWN_TOKEN,
                    message = "Unknown function: $name",
                    position = null,
                ),
            )
        }
    }

    private fun List<Double>.requireArity(expected: Int, name: String): Double {
        if (size != expected) {
            throw CalcException(
                CalcError(
                    code = CalcError.Code.INVALID_SYNTAX,
                    message = "$name requires $expected argument${if (expected == 1) "" else "s"}",
                    position = null,
                ),
            )
        }
        return this[0]
    }
}
