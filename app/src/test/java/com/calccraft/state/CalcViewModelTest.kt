package com.calccraft.state

import com.calccraft.domain.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalcViewModelTest {

    private lateinit var viewModel: CalcViewModel
    private lateinit var engine: CalculatorEngine

    @Before
    fun setUp() {
        // Using a real engine as it's pure domain logic with no external dependencies
        engine = CalculatorEngine()
        viewModel = CalcViewModel(engine)
    }

    @Test
    fun `initial state is empty`() {
        val state = viewModel.state.value
        assertEquals("", state.currentExpression)
        assertEquals("0", state.displayValue)
        assertTrue(state.history.isEmpty())
        assertNull(state.lastError)
    }

    @Test
    fun `intent AppendCharacter builds expression`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('1'))
        viewModel.onIntent(CalcIntent.AppendCharacter('+'))
        viewModel.onIntent(CalcIntent.AppendCharacter('2'))

        val state = viewModel.state.value
        assertEquals("1+2", state.currentExpression)
        assertEquals("1+2", state.displayValue)
    }

    @Test
    fun `intent AppendFunction builds expression`() {
        viewModel.onIntent(CalcIntent.AppendFunction("sqrt"))

        val state = viewModel.state.value
        assertEquals("sqrt(", state.currentExpression)
        assertEquals("sqrt(", state.displayValue)
    }

    @Test
    fun `intent Delete removes last character`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('1'))
        viewModel.onIntent(CalcIntent.AppendCharacter('2'))
        viewModel.onIntent(CalcIntent.AppendCharacter('3'))

        viewModel.onIntent(CalcIntent.Delete)

        val state = viewModel.state.value
        assertEquals("12", state.currentExpression)
        assertEquals("12", state.displayValue)

        viewModel.onIntent(CalcIntent.Delete)
        assertEquals("1", viewModel.state.value.currentExpression)

        viewModel.onIntent(CalcIntent.Delete)
        assertEquals("", viewModel.state.value.currentExpression)

        // Deleting from empty state should do nothing
        viewModel.onIntent(CalcIntent.Delete)
        assertEquals("", viewModel.state.value.currentExpression)
    }

    @Test
    fun `intent Clear resets expression and display`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('1'))
        viewModel.onIntent(CalcIntent.AppendCharacter('+'))
        viewModel.onIntent(CalcIntent.Evaluate) // Evaluate to have a result state

        viewModel.onIntent(CalcIntent.Clear)

        val state = viewModel.state.value
        assertEquals("", state.currentExpression)
        assertEquals("0", state.displayValue)
        assertNull(state.lastError)
    }

    @Test
    fun `intent Evaluate success updates display and history`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('2'))
        viewModel.onIntent(CalcIntent.AppendCharacter('+'))
        viewModel.onIntent(CalcIntent.AppendCharacter('3'))
        viewModel.onIntent(CalcIntent.Evaluate)

        val state = viewModel.state.value
        assertEquals("5", state.currentExpression)
        assertEquals("5", state.displayValue)
        assertNull(state.lastError)

        assertEquals(1, state.history.size)
        val historyEntry = state.history.first()
        assertEquals("2+3", historyEntry.expression)
        assertEquals("5", historyEntry.resultText)
        assertFalse(historyEntry.isError)
    }

    @Test
    fun `intent Evaluate success with decimals formats correctly`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('1'))
        viewModel.onIntent(CalcIntent.AppendCharacter('/'))
        viewModel.onIntent(CalcIntent.AppendCharacter('2'))
        viewModel.onIntent(CalcIntent.Evaluate)

        val state = viewModel.state.value
        assertEquals("0.5", state.currentExpression)
        assertEquals("0.5", state.displayValue)
    }


    @Test
    fun `intent Evaluate error updates display, error state, and history`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('5'))
        viewModel.onIntent(CalcIntent.AppendCharacter('/'))
        viewModel.onIntent(CalcIntent.AppendCharacter('0'))
        viewModel.onIntent(CalcIntent.Evaluate)

        val state = viewModel.state.value
        assertEquals("5/0", state.currentExpression) // Expression remains
        assertNotNull(state.lastError)
        assertEquals("DIVIDE_BY_ZERO", state.lastError?.code)
        assertEquals("Error: Division by zero", state.displayValue)

        assertEquals(1, state.history.size)
        val historyEntry = state.history.first()
        assertEquals("5/0", historyEntry.expression)
        assertEquals("Error: Division by zero", historyEntry.resultText)
        assertTrue(historyEntry.isError)
    }

    @Test
    fun `appending number after evaluation starts a new expression`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('1'))
        viewModel.onIntent(CalcIntent.AppendCharacter('+'))
        viewModel.onIntent(CalcIntent.AppendCharacter('1'))
        viewModel.onIntent(CalcIntent.Evaluate) // state is "2"

        viewModel.onIntent(CalcIntent.AppendCharacter('3')) // Should start a new expression "3"

        val state = viewModel.state.value
        assertEquals("3", state.currentExpression)
        assertEquals("3", state.displayValue)
    }

    @Test
    fun `appending operator after evaluation continues calculation`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('2'))
        viewModel.onIntent(CalcIntent.AppendCharacter('+'))
        viewModel.onIntent(CalcIntent.AppendCharacter('3'))
        viewModel.onIntent(CalcIntent.Evaluate) // state is "5"

        viewModel.onIntent(CalcIntent.AppendCharacter('*'))
        viewModel.onIntent(CalcIntent.AppendCharacter('2'))

        val state = viewModel.state.value
        assertEquals("5*2", state.currentExpression)
        assertEquals("5*2", state.displayValue)

        viewModel.onIntent(CalcIntent.Evaluate)
        assertEquals("10", viewModel.state.value.displayValue)
    }

    @Test
    fun `appending after error starts a new expression`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('5'))
        viewModel.onIntent(CalcIntent.AppendCharacter('/'))
        viewModel.onIntent(CalcIntent.AppendCharacter('0'))
        viewModel.onIntent(CalcIntent.Evaluate) // state is an error

        viewModel.onIntent(CalcIntent.AppendCharacter('1'))

        val state = viewModel.state.value
        assertEquals("1", state.currentExpression)
        assertEquals("1", state.displayValue)
        assertNull(state.lastError) // Error should be cleared
    }

    @Test
    fun `getHistoryAsText formats correctly`() {
        // Entry 1: Success
        viewModel.onIntent(CalcIntent.AppendCharacter('2'))
        viewModel.onIntent(CalcIntent.AppendCharacter('*'))
        viewModel.onIntent(CalcIntent.AppendCharacter('3'))
        viewModel.onIntent(CalcIntent.Evaluate)
        viewModel.onIntent(CalcIntent.Clear)

        // Entry 2: Error
        viewModel.onIntent(CalcIntent.AppendCharacter('1'))
        viewModel.onIntent(CalcIntent.AppendCharacter('/'))
        viewModel.onIntent(CalcIntent.AppendCharacter('0'))
        viewModel.onIntent(CalcIntent.Evaluate)

        val expectedText = """
            2*3 = 6
            1/0 = Error: Division by zero
        """.trimIndent()

        val actualText = viewModel.getHistoryAsText()

        assertEquals(expectedText, actualText)
    }

    @Test
    fun `ClearHistory intent clears history`() {
        viewModel.onIntent(CalcIntent.AppendCharacter('1'))
        viewModel.onIntent(CalcIntent.AppendCharacter('+'))
        viewModel.onIntent(CalcIntent.AppendCharacter('1'))
        viewModel.onIntent(CalcIntent.Evaluate)

        assertEquals(1, viewModel.state.value.history.size)

        viewModel.onIntent(CalcIntent.ClearHistory)

        assertTrue(viewModel.state.value.history.isEmpty())
    }
}
