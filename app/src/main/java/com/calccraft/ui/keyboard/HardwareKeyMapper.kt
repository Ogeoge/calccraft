package com.calccraft.ui.keyboard

import android.view.KeyEvent
import com.calccraft.state.CalcIntent

/**
 * A stateless utility object to map hardware keyboard events to `CalcIntent` actions.
 */
object HardwareKeyMapper {

    /**
     * Maps an Android [KeyEvent] to a corresponding [CalcIntent].
     *
     * @param keyEvent The hardware key event to map.
     * @return A [CalcIntent] if the key is mapped, or `null` if it's unhandled.
     */
    fun mapKeyEvent(keyEvent: KeyEvent): CalcIntent? {
        // We only care about the key down event to avoid double processing.
        if (keyEvent.action != KeyEvent.ACTION_DOWN) {
            return null
        }

        // Handle non-character keys by their key code first.
        keyEvent.keyCode.let {
            when (it) {
                KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_EQUALS -> return CalcIntent.Evaluate
                KeyEvent.KEYCODE_DEL -> return CalcIntent.Delete // Backspace
                KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_C -> return CalcIntent.Clear
                else -> { /* fall through to character mapping */ }
            }
        }

        // Handle printable characters using unicodeChar for better layout support.
        val char = keyEvent.unicodeChar.toChar()

        return when (char) {
            in '0'..'9' -> CalcIntent.AppendCharacter(char)
            '+', '-', '*', '/', '^', '%', '.', '(', ')' -> CalcIntent.AppendCharacter(char)
            // Potentially map letters to functions in the future, e.g., 's' for 'sin('
            // For now, we stick to the core requirements.
            else -> null // Unhandled character
        }
    }
}
