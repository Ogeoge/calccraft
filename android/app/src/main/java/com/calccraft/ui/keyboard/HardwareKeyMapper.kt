package com.calccraft.ui.keyboard

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import com.calccraft.state.CalculatorIntent

/**
 * Maps hardware keyboard events to [CalculatorIntent]s.
 * This provides a layer of abstraction between the raw key events and the application's business logic.
 */
object HardwareKeyMapper {

    /**
     * Maps a given [KeyEvent] to a corresponding [CalculatorIntent].
     *
     * @param event The hardware key event to map.
     * @return A [CalculatorIntent] if the key is recognized, otherwise `null`.
     */
    fun map(event: KeyEvent): CalculatorIntent? {
        return when (event.key) {
            // Digits (main keyboard and numpad)
            Key.Zero, Key.NumPad0 -> CalculatorIntent.Append("0")
            Key.One, Key.NumPad1 -> CalculatorIntent.Append("1")
            Key.Two, Key.NumPad2 -> CalculatorIntent.Append("2")
            Key.Three, Key.NumPad3 -> CalculatorIntent.Append("3")
            Key.Four, Key.NumPad4 -> CalculatorIntent.Append("4")
            Key.Five, Key.NumPad5 -> CalculatorIntent.Append("5")
            Key.Six, Key.NumPad6 -> CalculatorIntent.Append("6")
            Key.Seven, Key.NumPad7 -> CalculatorIntent.Append("7")
            Key.Eight, Key.NumPad8 -> CalculatorIntent.Append("8")
            Key.Nine, Key.NumPad9 -> CalculatorIntent.Append("9")

            // Operators (main keyboard and numpad)
            Key.Plus, Key.NumPadAdd -> CalculatorIntent.Append("+")
            Key.Minus, Key.NumPadSubtract -> CalculatorIntent.Append("-")
            Key.NumPadMultiply -> CalculatorIntent.Append("*")
            Key.Slash, Key.NumPadDivide -> CalculatorIntent.Append("/")


            // Parentheses



            // Decimal point (main keyboard and numpad)
            Key.Period, Key.NumPadDot -> CalculatorIntent.Append(".")

            // Core actions
            Key.Enter, Key.NumPadEnter, Key.Equals -> CalculatorIntent.Evaluate
            Key.Backspace -> CalculatorIntent.Delete
            Key.Escape -> CalculatorIntent.Clear
            // Also map the 'Delete' (forward delete) key to Clear for convenience
            Key.Delete -> CalculatorIntent.Clear

            // Any other key is not handled
            else -> null
        }
    }
}
