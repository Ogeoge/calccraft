package com.calccraft.domain.engine

/**
 * Converts an expression string into a list of [Token]s.
 * This class handles numbers, operators, functions, constants, and parentheses.
 * It also correctly distinguishes between binary subtraction and unary negation.
 */
object Tokenizer {

    private val functions = setOf("sin", "cos", "tan", "log", "sqrt", "pow")
    private val constants = setOf("pi", "e")

    /**
     * Tokenizes the given mathematical expression string.
     *
     * @param expression The raw expression string.
     * @return A list of [Token]s representing the expression.
     * @throws IllegalArgumentException if an unknown token or identifier is found.
     */
    fun tokenize(expression: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0

        while (i < expression.length) {
            val char = expression[i]

            when {
                char.isWhitespace() -> {
                    i++
                    continue
                }

                char.isDigit() || (char == '.' && i + 1 < expression.length && expression[i + 1].isDigit()) -> {
                    val (numberStr, length) = readNumber(expression, i)
                    tokens.add(Token.Number(numberStr.toDouble()))
                    i += length
                }

                char.isLetter() -> {
                    val (identifier, length) = readIdentifier(expression, i)
                    when {
                        identifier in functions -> tokens.add(Token.Function(identifier))
                        identifier in constants -> tokens.add(Token.Constant(identifier))
                        else -> throw IllegalArgumentException("Unknown identifier: $identifier")
                    }
                    i += length
                }

                char == '+' -> { tokens.add(Token.Operator.Add); i++ }
                char == '*' -> { tokens.add(Token.Operator.Multiply); i++ }
                char == '/' -> { tokens.add(Token.Operator.Divide); i++ }
                char == '%' -> { tokens.add(Token.Operator.Percent); i++ }
                char == '(' -> { tokens.add(Token.LeftParen); i++ }
                char == ')' -> { tokens.add(Token.RightParen); i++ }
                char == ',' -> { tokens.add(Token.Comma); i++ }

                char == '-' -> {
                    val lastToken = tokens.lastOrNull()
                    // A minus is unary if it's the first token, or if it follows an operator, left parenthesis, or comma.
                    if (lastToken == null || lastToken is Token.Operator || lastToken is Token.LeftParen || lastToken is Token.Comma) {
                        tokens.add(Token.Operator.UnaryMinus)
                    } else {
                        tokens.add(Token.Operator.Subtract)
                    }
                    i++
                }

                else -> throw IllegalArgumentException("Unknown character in expression: $char")
            }
        }
        return tokens
    }

    private fun readNumber(expression: String, startIndex: Int): Pair<String, Int> {
        val sb = StringBuilder()
        var i = startIndex
        var hasDecimal = false
        while (i < expression.length) {
            val char = expression[i]
            if (char.isDigit()) {
                sb.append(char)
            } else if (char == '.' && !hasDecimal) {
                sb.append(char)
                hasDecimal = true
            } else {
                break
            }
            i++
        }
        // Handle cases like ".5" by prepending a "0" if it starts with a dot.
        if (sb.isNotEmpty() && sb.first() == '.') {
            sb.insert(0, '0')
        }
        return Pair(sb.toString(), i - startIndex)
    }

    private fun readIdentifier(expression: String, startIndex: Int): Pair<String, Int> {
        val sb = StringBuilder()
        var i = startIndex
        while (i < expression.length && expression[i].isLetter()) {
            sb.append(expression[i])
            i++
        }
        return Pair(sb.toString(), i - startIndex)
    }
}
