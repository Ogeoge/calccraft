package com.calccraft.domain.engine

/**
 * Represents a token in a mathematical expression, produced by the [Tokenizer].
 * These tokens are the input for the [ShuntingYard] parser.
 */
sealed class Token {
    /** A numeric literal, e.g., 3.14. */
    data class Number(val value: Double) : Token()

    /** An operator with defined precedence and associativity. */
    sealed class Operator(val symbol: String, val precedence: Int, val isLeftAssociative: Boolean) : Token() {
        // Binary operators
        object Add : Operator("+", 2, true)
        object Subtract : Operator("-", 2, true)
        object Multiply : Operator("*", 3, true)
        object Divide : Operator("/", 3, true)

        // Unary/Postfix operators
        object UnaryMinus : Operator("~", 5, false) // Internal symbol '~' for negation, right-associative
        object Percent : Operator("%", 4, true) // Postfix, handled as left-associative in shunting-yard
    }

    /** A function identifier, e.g., "sin", "pow". */
    data class Function(val name: String) : Token()

    /** A constant identifier, e.g., "pi", "e". */
    data class Constant(val name: String) : Token()

    /** A left parenthesis '('. */
    object LeftParen : Token()

    /** A right parenthesis ')'. */
    object RightParen : Token()

    /** A comma ',', used for separating function arguments. */
    object Comma : Token()
}
