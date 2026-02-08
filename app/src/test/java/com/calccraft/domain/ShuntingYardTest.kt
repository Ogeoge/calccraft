package com.calccraft.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.Queue

class ShuntingYardTest {

    private val shuntingYard = ShuntingYard()

    private fun toRpnString(queue: Queue<Token>): String {
        return queue.joinToString(" ") { token ->
            when (token) {
                is Token.Number -> token.value.toString().removeSuffix(".0")
                is Token.Operator -> token.symbol
                is Token.Function -> token.name
                is Token.UnaryMinus -> "u-"
                is Token.Percent -> "%"
                // Parentheses should not be in the output queue
                is Token.LeftParen -> "("
                is Token.RightParen -> ")"
            }
        }.trim()
    }

    private fun runTest(input: List<Token>, expectedRpn: String) {
        val result = shuntingYard.convert(input)
        result.fold(
            onSuccess = { rpnQueue ->
                assertEquals(expectedRpn, toRpnString(rpnQueue))
            },
            onFailure = { error ->
                fail("ShuntingYard failed with error: ${error.message}")
            }
        )
    }

    private fun runErrorTest(input: List<Token>, expectedErrorCode: String) {
        val result = shuntingYard.convert(input)
        result.fold(
            onSuccess = { rpnQueue ->
                fail("ShuntingYard should have failed but produced: ${toRpnString(rpnQueue)}")
            },
            onFailure = { error ->
                assertEquals(expectedErrorCode, error.code)
            }
        )
    }

    @Test
    fun `simple addition`() {
        val tokens = listOf(Token.Number(1.0), Token.Operator.Plus, Token.Number(2.0))
        runTest(tokens, "1 2 +")
    }

    @Test
    fun `operator precedence`() {
        val tokens = listOf(
            Token.Number(3.0),
            Token.Operator.Plus,
            Token.Number(4.0),
            Token.Operator.Multiply,
            Token.Number(2.0)
        )
        runTest(tokens, "3 4 2 * +")
    }

    @Test
    fun `parentheses override precedence`() {
        val tokens = listOf(
            Token.LeftParen,
            Token.Number(3.0),
            Token.Operator.Plus,
            Token.Number(4.0),
            Token.RightParen,
            Token.Operator.Multiply,
            Token.Number(2.0)
        )
        runTest(tokens, "3 4 + 2 *")
    }

    @Test
    fun `left-associative operators`() {
        val tokens = listOf(
            Token.Number(8.0),
            Token.Operator.Minus,
            Token.Number(4.0),
            Token.Operator.Minus,
            Token.Number(2.0)
        )
        runTest(tokens, "8 4 - 2 -")
    }

    @Test
    fun `right-associative operator (exponentiation)`() {
        val tokens = listOf(
            Token.Number(2.0),
            Token.Operator.Exponent,
            Token.Number(3.0),
            Token.Operator.Exponent,
            Token.Number(2.0)
        )
        runTest(tokens, "2 3 2 ^ ^")
    }

    @Test
    fun `unary minus at start`() {
        val tokens = listOf(Token.UnaryMinus, Token.Number(3.0))
        runTest(tokens, "3 u-")
    }

    @Test
    fun `unary minus after operator`() {
        val tokens = listOf(
            Token.Number(5.0),
            Token.Operator.Multiply,
            Token.UnaryMinus,
            Token.Number(2.0)
        )
        runTest(tokens, "5 2 u- *")
    }

    @Test
    fun `unary minus inside parentheses`() {
        val tokens = listOf(
            Token.Number(5.0),
            Token.Operator.Multiply,
            Token.LeftParen,
            Token.UnaryMinus,
            Token.Number(2.0),
            Token.RightParen
        )
        runTest(tokens, "5 2 u- *")
    }

    @Test
    fun `simple function call`() {
        val tokens = listOf(
            Token.Function("sin"),
            Token.LeftParen,
            Token.Number(1.0),
            Token.RightParen
        )
        runTest(tokens, "1 sin")
    }

    @Test
    fun `function with expression`() {
        val tokens = listOf(
            Token.Function("sqrt"),
            Token.LeftParen,
            Token.Number(3.0),
            Token.Operator.Multiply,
            Token.Number(3.0),
            Token.RightParen
        )
        runTest(tokens, "3 3 * sqrt")
    }

    @Test
    fun `complex function and operators`() {
        val tokens = listOf(
            Token.Number(2.0),
            Token.Operator.Plus,
            Token.Function("cos"),
            Token.LeftParen,
            Token.Number(0.0),
            Token.RightParen
        )
        runTest(tokens, "2 0 cos +")
    }

    @Test
    fun `postfix percent operator`() {
        val tokens = listOf(Token.Number(50.0), Token.Percent)
        runTest(tokens, "50 %")
    }

    @Test
    fun `percent in an expression`() {
        val tokens = listOf(
            Token.Number(200.0),
            Token.Operator.Multiply,
            Token.Number(10.0),
            Token.Percent
        )
        runTest(tokens, "200 10 % *")
    }

    @Test
    fun `complex expression`() {
        // Expression: 3 + 4 * 2 / ( 1 - 5 ) ^ 2 ^ 3
        val tokens = listOf(
            Token.Number(3.0), Token.Operator.Plus, Token.Number(4.0), Token.Operator.Multiply, Token.Number(2.0),
            Token.Operator.Divide, Token.LeftParen, Token.Number(1.0), Token.Operator.Minus, Token.Number(5.0),
            Token.RightParen, Token.Operator.Exponent, Token.Number(2.0), Token.Operator.Exponent, Token.Number(3.0)
        )
        runTest(tokens, "3 4 2 * 1 5 - 2 3 ^ ^ / +")
    }

    @Test
    fun `another complex expression with functions and unary`() {
        // Expression: -sqrt(4) * (50% + sin(0))
        val tokens = listOf(
            Token.UnaryMinus, Token.Function("sqrt"), Token.LeftParen, Token.Number(4.0), Token.RightParen,
            Token.Operator.Multiply, Token.LeftParen, Token.Number(50.0), Token.Percent, Token.Operator.Plus,
            Token.Function("sin"), Token.LeftParen, Token.Number(0.0), Token.RightParen, Token.RightParen
        )
        runTest(tokens, "4 sqrt u- 50 % 0 sin + *")
    }

    @Test
    fun `mismatched left parenthesis`() {
        // (1 + 2
        val tokens = listOf(Token.LeftParen, Token.Number(1.0), Token.Operator.Plus, Token.Number(2.0))
        runErrorTest(tokens, "MISMATCHED_PARENTHESES")
    }

    @Test
    fun `mismatched right parenthesis`() {
        // 1 + 2)
        val tokens = listOf(Token.Number(1.0), Token.Operator.Plus, Token.Number(2.0), Token.RightParen)
        runErrorTest(tokens, "MISMATCHED_PARENTHESES")
    }

    @Test
    fun `function without parenthesis should fail`() {
        // sin 1
        val tokens = listOf(Token.Function("sin"), Token.Number(1.0))
        runErrorTest(tokens, "INVALID_SYNTAX") // A function must be followed by a left parenthesis
    }

    @Test
    fun `empty input should succeed with empty output`() {
        runTest(emptyList(), "")
    }
}
