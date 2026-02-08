package com.calccraft.domain

/**
 * Consistent error shape for the engine and UI.
 *
 * Contract codes:
 * EMPTY_EXPRESSION, INVALID_SYNTAX, UNKNOWN_TOKEN, MISMATCHED_PARENTHESES, DIVIDE_BY_ZERO, DOMAIN_ERROR.
 */
sealed class CalcError(
    val code: String,
    val message: String,
    val position: Int? = null,
) {
    class EmptyExpression : CalcError(
        code = "EMPTY_EXPRESSION",
        message = "Enter an expression",
    )

    class InvalidSyntax(position: Int? = null, messageOverride: String? = null) : CalcError(
        code = "INVALID_SYNTAX",
        message = messageOverride ?: "Invalid syntax",
        position = position,
    )

    class UnknownToken(token: String, position: Int? = null) : CalcError(
        code = "UNKNOWN_TOKEN",
        message = "Unknown token: $token",
        position = position,
    )

    class MismatchedParentheses(position: Int? = null) : CalcError(
        code = "MISMATCHED_PARENTHESES",
        message = "Mismatched parentheses",
        position = position,
    )

    class DivideByZero(position: Int? = null) : CalcError(
        code = "DIVIDE_BY_ZERO",
        message = "Division by zero",
        position = position,
    )

    class DomainError(details: String? = null, position: Int? = null) : CalcError(
        code = "DOMAIN_ERROR",
        message = details?.let { "Domain error: $it" } ?: "Domain error",
        position = position,
    )
}
