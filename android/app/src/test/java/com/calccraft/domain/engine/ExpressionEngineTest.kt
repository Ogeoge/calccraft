package com.calccraft.domain.engine

import com.calccraft.domain.model.EvalResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log
import kotlin.math.sin
import kotlin.math.sqrt

class ExpressionEngineTest {

    private val engine = ExpressionEngine()
    private val delta = 1e-9

    private fun assertSuccess(expression: String, expectedValue: Double) {
        val result = engine.evaluate(expression)
        assertTrue("Expected success for '$expression', but was $result", result is EvalResult.Success)
        val successResult = result as EvalResult.Success
        assertEquals(expectedValue, successResult.value, delta)
    }

    private fun assertError(expression: String, expectedErrorKind: String) {
        val result = engine.evaluate(expression)
        assertTrue("Expected error for '$expression', but was $result", result is EvalResult.Error)
        val errorResult = result as EvalResult.Error
        assertEquals(expectedErrorKind, errorResult.errorKind)
    }

    @Test
    fun `evaluates basic arithmetic`() {
        assertSuccess("1 + 1", 2.0)
        assertSuccess("5 - 3", 2.0)
        assertSuccess("2 * 3", 6.0)
        assertSuccess("10 / 2", 5.0)
        assertSuccess("5 / 2", 2.5)
    }

    @Test
    fun `handles operator precedence correctly`() {
        assertSuccess("2 + 3 * 4", 14.0)
        assertSuccess("10 - 6 / 2", 7.0)
        assertSuccess("2*3+4*5", 26.0)
    }

    @Test
    fun `handles parentheses correctly`() {
        assertSuccess("(2 + 3) * 4", 20.0)
        assertSuccess("10 - (6 / 2)", 7.0)
        assertSuccess("2*(3+4)*5", 70.0)
    }

    @Test
    fun `handles decimals correctly`() {
        assertSuccess("1.5 * 2.5", 3.75)
        assertSuccess("10.0 / 4.0", 2.5)
        assertSuccess(".5 * 2", 1.0)
    }

    @Test
    fun `handles unary minus correctly`() {
        assertSuccess("-5", -5.0)
        assertSuccess("10 * -2", -20.0)
        assertSuccess("-(2 + 3)", -5.0)
        assertSuccess("5 - -3", 8.0)
        assertSuccess("-(-5)", 5.0)
    }

    @Test
    fun `handles percent operator correctly`() {
        assertSuccess("50%", 0.5)
        assertSuccess("200 * 10%", 20.0)
        assertSuccess("1 + 5%", 1.05)
        assertSuccess("(10+10)%", 0.2)
    }

    @Test
    fun `evaluates constants correctly`() {
        assertSuccess("pi", PI)
        assertSuccess("e", E)
        assertSuccess("2 * pi", 2 * PI)
    }

    @Test
    fun `evaluates functions correctly`() {
        assertSuccess("sqrt(16)", 4.0)
        assertSuccess("pow(2, 3)", 8.0)
        assertSuccess("sin(0)", 0.0)
        assertSuccess("cos(pi)", -1.0)
        assertSuccess("log(e)", 1.0)
        assertSuccess("tan(0)", 0.0)
    }

    @Test
    fun `evaluates nested functions and expressions`() {
        assertSuccess("sqrt(pow(2, 4))", 4.0)
        assertSuccess("2 * (3 + sin(pi/2))", 8.0) // sin(pi/2) is 1
        assertSuccess("log(pow(e, 2))", 2.0)
    }

    @Test
    fun `handles complex expressions`() {
        assertSuccess("-pi * (2 + sqrt(16))", -PI * (2 + 4))
        assertSuccess("100 / pow(2, 3) - 0.5", 100 / 8.0 - 0.5)
    }

    @Test
    fun `handles division by zero`() {
        assertError("1 / 0", "DivideByZero")
        assertError("1 / (2 - 2)", "DivideByZero")
        assertError("5 % / 0", "DivideByZero")
    }

    @Test
    fun `handles domain errors`() {
        assertError("sqrt(-1)", "DomainError")
        assertError("log(0)", "DomainError")
        assertError("log(-5)", "DomainError")
    }

    @Test
    fun `handles invalid syntax`() {
        assertError("1 +", "InvalidSyntax")
        assertError("* 2", "InvalidSyntax")
        assertError("1 2", "InvalidSyntax")
        assertError("1 * * 2", "InvalidSyntax")
        assertError("()", "InvalidSyntax")
        assertError("pow(2,)", "InvalidSyntax")
    }

    @Test
    fun `handles mismatched parentheses`() {
        assertError("(1 + 2", "MismatchedParentheses")
        assertError("1 + 2)", "MismatchedParentheses")
        assertError("((1)", "MismatchedParentheses")
    }

    @Test
    fun `handles unknown tokens`() {
        assertError("1 $ 2", "UnknownToken")
        assertError("abc", "UnknownToken")
        assertError("1 # 2", "UnknownToken")
    }

    @Test
    fun `handles function argument errors`() {
        assertError("pow(2)", "StackUnderflow") // Expects 2 args, gets 1
        assertError("sin()", "InvalidSyntax") // Empty parens
        assertError("pow(2,3,4)", "InvalidSyntax") // Too many args
        assertError("sqrt(4, 5)", "InvalidSyntax") // Too many args
    }

    @Test
    fun `check formatted output for integers and decimals`() {
        val resultInt = engine.evaluate("2+2") as EvalResult.Success
        assertEquals("4", resultInt.formatted)

        val resultDecimal = engine.evaluate("1/4") as EvalResult.Success
        assertEquals("0.25", resultDecimal.formatted)

        val resultTrailingZero = engine.evaluate("1.0 + 2.0") as EvalResult.Success
        assertEquals("3", resultTrailingZero.formatted)

        val resultLongDecimal = engine.evaluate("1/3")
        // Don't assert exact string due to platform differences, but check it's a success
        assertTrue(resultLongDecimal is EvalResult.Success)
    }
}
