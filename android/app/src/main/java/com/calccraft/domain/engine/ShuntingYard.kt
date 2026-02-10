package com.calccraft.domain.engine

import java.util.Stack

/**
 * Implements the Shunting-yard algorithm to convert an infix token stream
 * to a postfix (Reverse Polish Notation) token queue.
 *
 * This RPN output is then used by the [RpnEvaluator].
 */
object ShuntingYard {

    /**
     * Converts a list of tokens in infix notation to RPN.
     *
     * @param tokens The list of [Token]s from the [Tokenizer].
     * @return A list of [Token]s in RPN order.
     * @throws IllegalArgumentException for syntax errors like mismatched parentheses.
     */
    fun toRpn(tokens: List<Token>): List<Token> {
        val outputQueue: MutableList<Token> = mutableListOf()
        val operatorStack: Stack<Token> = Stack()

        for (token in tokens) {
            when (token) {
                is Token.Number, is Token.Constant -> {
                    outputQueue.add(token)
                }
                is Token.Function -> {
                    operatorStack.push(token)
                }
                is Token.Comma -> {
                    // Pop operators from the stack to the output until a left parenthesis is found.
                    while (operatorStack.isNotEmpty() && operatorStack.peek() !is Token.LeftParen) {
                        outputQueue.add(operatorStack.pop())
                    }
                    // If the stack is empty, the comma is misplaced.
                    if (operatorStack.isEmpty()) {
                        throw IllegalArgumentException("Invalid syntax: Misplaced comma or mismatched parentheses.")
                    }
                }
                is Token.Operator -> {
                    // While there's an operator on the stack with higher or equal precedence, pop it.
                    while (
                        operatorStack.isNotEmpty() &&
                        operatorStack.peek() is Token.Operator &&
                        hasHigherOrEqualPrecedence(operatorStack.peek() as Token.Operator, token)
                    ) {
                        outputQueue.add(operatorStack.pop())
                    }
                    operatorStack.push(token)
                }
                is Token.LeftParen -> {
                    operatorStack.push(token)
                }
                is Token.RightParen -> {
                    while (operatorStack.isNotEmpty() && operatorStack.peek() !is Token.LeftParen) {
                        outputQueue.add(operatorStack.pop())
                    }
                    // If the stack runs out without finding a left paren, parentheses are mismatched.
                    if (operatorStack.isEmpty()) {
                        throw IllegalArgumentException("Mismatched parentheses: Missing '('.")
                    }
                    // Pop the left parenthesis from the stack.
                    operatorStack.pop()

                    // If the token at the top of the stack is a function, pop it to the output.
                    if (operatorStack.isNotEmpty() && operatorStack.peek() is Token.Function) {
                        outputQueue.add(operatorStack.pop())
                    }
                }
            }
        }

        // Pop any remaining operators from the stack to the output queue.
        while (operatorStack.isNotEmpty()) {
            val op = operatorStack.pop()
            if (op is Token.LeftParen) {
                // If a left paren is found here, parentheses are mismatched.
                throw IllegalArgumentException("Mismatched parentheses: Missing ')'.")
            }
            outputQueue.add(op)
        }

        return outputQueue
    }

    /**
     * Compares the precedence of two operators.
     *
     * @param opOnStack The operator on top of the stack.
     * @param currentOp The current operator being processed.
     * @return `true` if `opOnStack` has higher precedence, or equal precedence and `currentOp` is left-associative.
     */
    private fun hasHigherOrEqualPrecedence(opOnStack: Token.Operator, currentOp: Token.Operator): Boolean {
        val p1 = opOnStack.precedence
        val p2 = currentOp.precedence
        return (p1 > p2) || (p1 == p2 && currentOp.isLeftAssociative)
    }
}
