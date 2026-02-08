package com.calccraft.domain

import java.util.Locale

/**
 * Safe tokenizer for CalcCraft.
 *
 * Supported:
 * - Numbers (integer/decimal)
 * - Operators: + - * / ^
 * - Parentheses: ( )
 * - Postfix percent: %
 * - Sqrt:  (as function token) and "sqrt" identifier
 * - Functions: sin, cos, tan, log (case-insensitive)
 *
 * Notes:
 * - Unary minus is not emitted here as a separate token; it is represented as an Operator.MINUS
 *   and should be interpreted as unary by ShuntingYard based on token context.
 */
object Tokenizer {

    fun tokenize(expression: String): Result<List<Token>> {
        val input = expression.trim()
        if (input.isEmpty()) {
            return Result.failure(
                CalcErrorException(
                    CalcError(
                        code = CalcErrorCode.EMPTY_EXPRESSION,
                        message = "Expression is empty",
                        position = null,
                    ),
                ),
            )
        }

        val tokens = mutableListOf<Token>()
        var i = 0

        fun fail(code: CalcErrorCode, message: String, position: Int?): Result<List<Token>> {
            return Result.failure(CalcErrorException(CalcError(code = code, message = message, position = position)))
        }

        while (i < input.length) {
            val ch = input[i]

            when {
                ch.isWhitespace() -> {
                    i++
                }

                ch.isDigit() || ch == '.' -> {
                    val start = i
                    var seenDot = false
                    if (ch == '.') {
                        seenDot = true
                        // leading '.' must be followed by a digit
                        if (i + 1 >= input.length || !input[i + 1].isDigit()) {
                            return fail(
                                CalcErrorCode.INVALID_SYNTAX,
                                "Invalid number format",
                                start,
                            )
                        }
                    }

                    i++
                    while (i < input.length) {
                        val c = input[i]
                        if (c.isDigit()) {
                            i++
                            continue
                        }
                        if (c == '.') {
                            if (seenDot) {
                                return fail(
                                    CalcErrorCode.INVALID_SYNTAX,
                                    "Invalid number format",
                                    i,
                                )
                            }
                            seenDot = true
                            i++
                            continue
                        }
                        break
                    }

                    val raw = input.substring(start, i)
                    val value = raw.toDoubleOrNull()
                        ?: return fail(
                            CalcErrorCode.INVALID_SYNTAX,
                            "Invalid number '$raw'",
                            start,
                        )

                    tokens.add(Token.Number(value, startIndex = start))
                }

                ch == '(' -> {
                    tokens.add(Token.LeftParen(index = i))
                    i++
                }

                ch == ')' -> {
                    tokens.add(Token.RightParen(index = i))
                    i++
                }

                ch == '%' -> {
                    tokens.add(Token.Percent(index = i))
                    i++
                }

                ch == '' -> {
                    // sqrt symbol
                    tokens.add(Token.Function(name = "sqrt", startIndex = i))
                    i++
                }

                ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '^' -> {
                    val op = when (ch) {
                        '+' -> Operator.PLUS
                        '-' -> Operator.MINUS
                        '*' -> Operator.TIMES
                        '/' -> Operator.DIV
                        '^' -> Operator.POW
                        else -> return fail(
                            CalcErrorCode.UNKNOWN_TOKEN,
                            "Unknown operator '$ch'",
                            i,
                        )
                    }
                    tokens.add(Token.Op(op, index = i))
                    i++
                }

                ch.isLetter() -> {
                    val start = i
                    i++
                    while (i < input.length && input[i].isLetter()) {
                        i++
                    }
                    val ident = input.substring(start, i).lowercase(Locale.US)
                    when (ident) {
                        "sin", "cos", "tan", "log", "sqrt" -> {
                            tokens.add(Token.Function(name = ident, startIndex = start))
                        }

                        else -> {
                            return fail(
                                CalcErrorCode.UNKNOWN_TOKEN,
                                "Unknown identifier '$ident'",
                                start,
                            )
                        }
                    }
                }

                else -> {
                    return fail(
                        CalcErrorCode.UNKNOWN_TOKEN,
                        "Unknown token '$ch'",
                        i,
                    )
                }
            }
        }

        return Result.success(tokens)
    }
}
