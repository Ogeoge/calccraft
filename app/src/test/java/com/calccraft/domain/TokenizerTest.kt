package com.calccraft.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class TokenizerTest {

    @Test
    fun `tokenize single number`() {
        val tokens = Tokenizer.tokenize("123")
        assertEquals(listOf(Token.Number(123.0)), tokens)
    }

    @Test
    fun `tokenize decimal number`() {
        val tokens = Tokenizer.tokenize("3.14159")
        assertEquals(listOf(Token.Number(3.14159)), tokens)
    }

    @Test
    fun `tokenize number starting with a dot`() {
        val tokens = Tokenizer.tokenize(".5")
        assertEquals(listOf(Token.Number(0.5)), tokens)
    }

    @Test
    fun `tokenize number ending with a dot`() {
        val tokens = Tokenizer.tokenize("42.")
        assertEquals(listOf(Token.Number(42.0)), tokens)
    }

    @Test
    fun `tokenize simple addition`() {
        val tokens = Tokenizer.tokenize("1 + 2")
        val expected = listOf(
            Token.Number(1.0),
            Token.Operator(OperatorType.ADD),
            Token.Number(2.0)
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize complex expression with all operators`() {
        val expression = "3+4*2/(1-5)^2"
        val expected = listOf(
            Token.Number(3.0),
            Token.Operator(OperatorType.ADD),
            Token.Number(4.0),
            Token.Operator(OperatorType.MULTIPLY),
            Token.Number(2.0),
            Token.Operator(OperatorType.DIVIDE),
            Token.LeftParen,
            Token.Number(1.0),
            Token.Operator(OperatorType.SUBTRACT),
            Token.Number(5.0),
            Token.RightParen,
            Token.Operator(OperatorType.POWER),
            Token.Number(2.0)
        )
        assertEquals(expected, Tokenizer.tokenize(expression))
    }

    @Test
    fun `tokenize unary minus at start`() {
        val tokens = Tokenizer.tokenize("-5")
        val expected = listOf(Token.Operator(OperatorType.UNARY_MINUS), Token.Number(5.0))
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize unary minus after operator`() {
        val tokens = Tokenizer.tokenize("10 * -2")
        val expected = listOf(
            Token.Number(10.0),
            Token.Operator(OperatorType.MULTIPLY),
            Token.Operator(OperatorType.UNARY_MINUS),
            Token.Number(2.0)
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize unary minus after left parenthesis`() {
        val tokens = Tokenizer.tokenize("(-5)")
        val expected = listOf(
            Token.LeftParen,
            Token.Operator(OperatorType.UNARY_MINUS),
            Token.Number(5.0),
            Token.RightParen
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize binary minus (subtraction)`() {
        val tokens = Tokenizer.tokenize("10 - 5")
        val expected = listOf(
            Token.Number(10.0),
            Token.Operator(OperatorType.SUBTRACT),
            Token.Number(5.0)
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize percent operator`() {
        val tokens = Tokenizer.tokenize("50%")
        val expected = listOf(
            Token.Number(50.0),
            Token.Operator(OperatorType.PERCENT)
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize functions`() {
        val tokens = Tokenizer.tokenize("sin(cos(0))")
        val expected = listOf(
            Token.Function("sin"),
            Token.LeftParen,
            Token.Function("cos"),
            Token.LeftParen,
            Token.Number(0.0),
            Token.RightParen,
            Token.RightParen
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize sqrt function name`() {
        val tokens = Tokenizer.tokenize("sqrt(9)")
        val expected = listOf(
            Token.Function("sqrt"),
            Token.LeftParen,
            Token.Number(9.0),
            Token.RightParen
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize sqrt symbol`() {
        val tokens = Tokenizer.tokenize("√9")
        val expected = listOf(
            Token.Function("sqrt"),
            Token.Number(9.0)
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize expression with multiple functions`() {
        val tokens = Tokenizer.tokenize("log(10) + tan(45)")
        val expected = listOf(
            Token.Function("log"),
            Token.LeftParen,
            Token.Number(10.0),
            Token.RightParen,
            Token.Operator(OperatorType.ADD),
            Token.Function("tan"),
            Token.LeftParen,
            Token.Number(45.0),
            Token.RightParen
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize expression with whitespace`() {
        val tokens = Tokenizer.tokenize("  10   /  2 ")
        val expected = listOf(
            Token.Number(10.0),
            Token.Operator(OperatorType.DIVIDE),
            Token.Number(2.0)
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize empty string returns empty list`() {
        val tokens = Tokenizer.tokenize("")
        assertEquals(emptyList<Token>(), tokens)
    }

    @Test
    fun `tokenize whitespace string returns empty list`() {
        val tokens = Tokenizer.tokenize("   \t \n ")
        assertEquals(emptyList<Token>(), tokens)
    }

    @Test
    fun `throws exception for unknown token`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Tokenizer.tokenize("10 $ 2")
        }
        assertEquals("Unknown token: '$' at position 3", exception.message)
    }

    @Test
    fun `throws exception for multiple decimal points`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Tokenizer.tokenize("1.2.3")
        }
        assertEquals("Invalid number format while tokenizing", exception.message)
    }

    @Test
    fun `function name not followed by parenthesis should be unknown`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Tokenizer.tokenize("sin cos")
        }
        assertEquals("Unknown token: 'c' at position 4", exception.message)
    }
}
