package com.calccraft.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class RpnEvaluatorTest {

    private val evaluator = RpnEvaluator()

    @Test
    fun `evaluate simple addition`() {
        val tokens = listOf(Token.Number(1.0), Token.Number(2.0), Token.Operator(OperatorType.PLUS))
        val result = evaluator.evaluate(tokens)
        assertEquals(3.0, result.value)
        assertNull(result.error)
    }

    @Test
    fun `evaluate complex expression`() {
        // Corresponds to infix: 5 + (1 + 2) * 4 - 3
        val tokens = listOf(
            Token.Number(5.0),
            Token.Number(1.0),
            Token.Number(2.0),
            Token.Operator(OperatorType.PLUS),
            Token.Number(4.0),
            Token.Operator(OperatorType.MULTIPLY),
            Token.Operator(OperatorType.PLUS),
            Token.Number(3.0),
            Token.Operator(OperatorType.MINUS)
        )
        val result = evaluator.evaluate(tokens)
        assertEquals(14.0, result.value)
    }

    @Test
    fun `evaluate with unary minus`() {
        // Corresponds to infix: -5 * 2
        val tokens = listOf(
            Token.Number(5.0),
            Token.Operator(OperatorType.UNARY_MINUS),
            Token.Number(2.0),
            Token.Operator(OperatorType.MULTIPLY)
        )
        val result = evaluator.evaluate(tokens)
        assertEquals(-10.0, result.value)
    }

    @Test
    fun `evaluate exponentiation`() {
        // Corresponds to infix: 2 ^ 3
        val tokens = listOf(Token.Number(2.0), Token.Number(3.0), Token.Operator(OperatorType.POWER))
        val result = evaluator.evaluate(tokens)
        assertEquals(8.0, result.value)
    }

    @Test
    fun `evaluate percent`() {
        // Corresponds to infix: 75%
        val tokens = listOf(Token.Number(75.0), Token.Operator(OperatorType.PERCENT))
        val result = evaluator.evaluate(tokens)
        assertEquals(0.75, result.value)
    }

    @Test
    fun `evaluate sqrt function`() {
        val tokens = listOf(Token.Number(16.0), Token.Function(FunctionType.SQRT))
        val result = evaluator.evaluate(tokens)
        assertEquals(4.0, result.value)
    }

    @Test
    fun `evaluate sin function`() {
        val tokens = listOf(Token.Number(0.0), Token.Function(FunctionType.SIN))
        val result = evaluator.evaluate(tokens)
        assertEquals(sin(0.0), result.value)
    }

    @Test
    fun `evaluate cos function`() {
        val tokens = listOf(Token.Number(0.0), Token.Function(FunctionType.COS))
        val result = evaluator.evaluate(tokens)
        assertEquals(cos(0.0), result.value)
    }

    @Test
    fun `evaluate tan function`() {
        val tokens = listOf(Token.Number(0.0), Token.Function(FunctionType.TAN))
        val result = evaluator.evaluate(tokens)
        assertEquals(tan(0.0), result.value)
    }

    @Test
    fun `evaluate log function`() {
        // Assuming log is base 10
        val tokens = listOf(Token.Number(100.0), Token.Function(FunctionType.LOG))
        val result = evaluator.evaluate(tokens)
        assertEquals(2.0, result.value)
    }

    // --- Error Cases ---

    @Test
    fun `evaluate divide by zero returns error`() {
        val tokens = listOf(Token.Number(1.0), Token.Number(0.0), Token.Operator(OperatorType.DIVIDE))
        val result = evaluator.evaluate(tokens)
        assertNull(result.value)
        assertNotNull(result.error)
        assertEquals("DIVIDE_BY_ZERO", result.error?.code)
    }

    @Test
    fun `evaluate sqrt of negative number returns domain error`() {
        val tokens = listOf(Token.Number(-4.0), Token.Function(FunctionType.SQRT))
        val result = evaluator.evaluate(tokens)
        assertNull(result.value)
        assertNotNull(result.error)
        assertEquals("DOMAIN_ERROR", result.error?.code)
    }

    @Test
    fun `evaluate log of zero returns domain error`() {
        val tokens = listOf(Token.Number(0.0), Token.Function(FunctionType.LOG))
        val result = evaluator.evaluate(tokens)
        assertNull(result.value)
        assertNotNull(result.error)
        assertEquals("DOMAIN_ERROR", result.error?.code)
    }

    @Test
    fun `evaluate log of negative number returns domain error`() {
        val tokens = listOf(Token.Number(-10.0), Token.Function(FunctionType.LOG))
        val result = evaluator.evaluate(tokens)
        assertNull(result.value)
        assertNotNull(result.error)
        assertEquals("DOMAIN_ERROR", result.error?.code)
    }

    @Test
    fun `evaluate with too few operands returns syntax error`() {
        // Corresponds to an invalid RPN from an expression like: 5 *
        val tokens = listOf(Token.Number(5.0), Token.Operator(OperatorType.MULTIPLY))
        val result = evaluator.evaluate(tokens)
        assertNull(result.value)
        assertNotNull(result.error)
        assertEquals("INVALID_SYNTAX", result.error?.code)
    }

    @Test
    fun `evaluate with too many operands returns syntax error`() {
        // Corresponds to an invalid RPN from an expression like: 5 2
        val tokens = listOf(Token.Number(5.0), Token.Number(2.0))
        val result = evaluator.evaluate(tokens)
        assertNull(result.value)
        assertNotNull(result.error)
        assertEquals("INVALID_SYNTAX", result.error?.code)
    }

    @Test
    fun `evaluate empty token list returns empty expression error`() {
        val tokens = emptyList<Token>()
        val result = evaluator.evaluate(tokens)
        assertNull(result.value)
        assertNotNull(result.error)
        // This could be INVALID_SYNTAX or a more specific error.
        // EMPTY_EXPRESSION is handled at a higher level (CalculatorEngine).
        // For the evaluator, an empty list is a form of invalid syntax.
        assertEquals("INVALID_SYNTAX", result.error?.code)
    }
}
