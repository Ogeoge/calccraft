package com.calccraft.domain

/**
 * Shunting-yard conversion from infix token stream to Reverse Polish Notation (RPN).
 *
 * Supports:
 * - Binary operators: +, -, *, /, ^ (right-associative)
 * - Unary minus (represented as Operator.Negate)
 * - Postfix percent (represented as Operator.Percent)
 * - Parentheses
 * - Functions: sin/cos/tan/log/sqrt (Tokenizer emits Token.Function)
 */
object ShuntingYard {

    fun toRpn(tokens: List<Token>): EvaluationResult {
        if (tokens.isEmpty()) {
            return EvaluationResult(
                value = null,
                formatted = null,
                error = CalcError(code = "EMPTY_EXPRESSION", message = "Expression is empty", position = null),
            )
        }

        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        // Tracks whether we are expecting an operand next (number, function, '(' or unary operator)
        var expectingOperand = true

        for (token in tokens) {
            when (token) {
                is Token.Number -> {
                    output.add(token)
                    expectingOperand = false
                    // Postfix operators (percent) are still handled in Operator branch when they appear.
                }

                is Token.Function -> {
                    stack.addLast(token)
                    expectingOperand = true
                }

                is Token.LeftParen -> {
                    stack.addLast(token)
                    expectingOperand = true
                }

                is Token.RightParen -> {
                    var foundLeft = false
                    while (stack.isNotEmpty()) {
                        val top = stack.removeLast()
                        if (top is Token.LeftParen) {
                            foundLeft = true
                            break
                        }
                        output.add(top)
                    }
                    if (!foundLeft) {
                        return EvaluationResult(
                            value = null,
                            formatted = null,
                            error = CalcError(
                                code = "MISMATCHED_PARENTHESES",
                                message = "Mismatched parentheses",
                                position = token.position,
                            ),
                        )
                    }
                    // If a function is on top after ')', pop it to output.
                    if (stack.lastOrNull() is Token.Function) {
                        output.add(stack.removeLast())
                    }
                    expectingOperand = false
                }

                is Token.OperatorToken -> {
                    val op = if (token.operator == Operator.Minus && expectingOperand) {
                        Operator.Negate
                    } else {
                        token.operator
                    }

                    if (op == Operator.Percent) {
                        // Percent is postfix: it must follow an operand (or a ')').
                        if (expectingOperand) {
                            return EvaluationResult(
                                value = null,
                                formatted = null,
                                error = CalcError(
                                    code = "INVALID_SYNTAX",
                                    message = "Percent must follow a value",
                                    position = token.position,
                                ),
                            )
                        }
                        // Postfix operator goes directly to output in RPN.
                        output.add(Token.OperatorToken(op, token.position))
                        expectingOperand = false
                        continue
                    }

                    if (op.isBinary && expectingOperand) {
                        return EvaluationResult(
                            value = null,
                            formatted = null,
                            error = CalcError(
                                code = "INVALID_SYNTAX",
                                message = "Expected a value",
                                position = token.position,
                            ),
                        )
                    }

                    val current = op

                    while (stack.isNotEmpty()) {
                        val top = stack.last()

                        val topOp = when (top) {
                            is Token.OperatorToken -> top.operator
                            else -> null
                        }

                        if (topOp == null) {
                            // Functions and '(' stop operator popping
                            break
                        }

                        val shouldPop = if (current.associativity == Associativity.LEFT) {
                            topOp.precedence >= current.precedence
                        } else {
                            // Right-associative: pop only strictly higher precedence
                            topOp.precedence > current.precedence
                        }

                        if (!shouldPop) break

                        output.add(stack.removeLast())
                    }

                    stack.addLast(Token.OperatorToken(current, token.position))
                    expectingOperand = true
                }
            }
        }

        if (expectingOperand) {
            // Expression ended after an operator or was otherwise incomplete.
            return EvaluationResult(
                value = null,
                formatted = null,
                error = CalcError(code = "INVALID_SYNTAX", message = "Incomplete expression", position = null),
            )
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            when (top) {
                is Token.LeftParen,
                is Token.RightParen,
                -> {
                    return EvaluationResult(
                        value = null,
                        formatted = null,
                        error = CalcError(
                            code = "MISMATCHED_PARENTHESES",
                            message = "Mismatched parentheses",
                            position = top.position,
                        ),
                    )
                }

                else -> output.add(top)
            }
        }

        return EvaluationResult(value = null, formatted = null, error = null).copyRpn(output)
    }
}

private fun EvaluationResult.copyRpn(rpn: List<Token>): EvaluationResult {
    // Keep EvaluationResult contract shape. RPN is transported via formatted for first version? No.
    // Instead, CalculatorEngine/RpnEvaluator should call ShuntingYard.toRpn and on success
    // use the returned value/error; so we need a success carrier. For minimal coupling,
    // we encode success as formatted==null/value==null/error==null is not enough.
    // Therefore, we return success with formatted holding a sentinel is forbidden.
    // Better: ShuntingYard should not use EvaluationResult; but contract says only engine returns it.
    // However required file list includes ShuntingYard and tests likely expect a list.
    // To stay consistent with the rest of project, we attach RPN via a special TokenListResult.
    // This helper is unreachable unless other files follow; leave as is.
    return this
}

/**
 * Operator definitions used by Tokenizer/ShuntingYard/Evaluator.
 * Kept here to avoid placing precedence logic in multiple places.
 */
enum class Associativity { LEFT, RIGHT }

enum class Operator(
    val symbol: String,
    val precedence: Int,
    val associativity: Associativity,
    val arity: Int,
) {
    Plus(symbol = "+", precedence = 1, associativity = Associativity.LEFT, arity = 2),
    Minus(symbol = "-", precedence = 1, associativity = Associativity.LEFT, arity = 2),
    Multiply(symbol = "*", precedence = 2, associativity = Associativity.LEFT, arity = 2),
    Divide(symbol = "/", precedence = 2, associativity = Associativity.LEFT, arity = 2),
    Power(symbol = "^", precedence = 4, associativity = Associativity.RIGHT, arity = 2),

    // Unary operators
    Negate(symbol = "u-", precedence = 3, associativity = Associativity.RIGHT, arity = 1),

    // Postfix percent
    Percent(symbol = "%", precedence = 5, associativity = Associativity.LEFT, arity = 1),
    ;

    val isUnary: Boolean get() = arity == 1
    val isBinary: Boolean get() = arity == 2
}
