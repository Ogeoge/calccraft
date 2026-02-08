package com.calccraft.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calccraft.ui.theme.CalcCraftTheme

/**
 * A reusable, circular button for the calculator keypad.
 *
 * @param symbol The text symbol to display on the button (e.g., "7", "+").
 * @param modifier The modifier to be applied to the button's container.
 * @param color The background color of the button surface.
 * @param contentColor The color of the text symbol.
 * @param onClick The callback invoked when the button is clicked.
 */
@Composable
fun CalcButton(
    symbol: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit
) {
    val contentDesc = when (symbol) {
        "+" -> "Plus"
        "-" -> "Minus"
        "*" -> "Multiply"
        "/" -> "Divide"
        "=" -> "Equals"
        "C" -> "Clear"
        "AC" -> "All Clear"
        "⌫" -> "Backspace"
        "%" -> "Percent"
        "^" -> "Power"
        "√" -> "Square Root"
        "." -> "Decimal Point"
        else -> symbol
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(8.dp)
            .clip(CircleShape)
            .clickable(
                role = Role.Button,
                onClickLabel = contentDesc,
                onClick = onClick
            )
            .aspectRatio(1f) // Ensures the clickable area is square before clipping
    ) {
        Surface(
            shape = CircleShape,
            color = color,
            contentColor = contentColor,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics {
                        // This ensures screen readers announce the descriptive name
                        this.contentDescription = contentDesc
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 80, heightDp = 80)
@Composable
private fun CalcButtonPreview() {
    CalcCraftTheme {
        CalcButton(symbol = "7", onClick = {})
    }
}

@Preview(showBackground = true, widthDp = 80, heightDp = 80)
@Composable
private fun CalcOperatorButtonPreview() {
    CalcCraftTheme {
        CalcButton(
            symbol = "+",
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 80, heightDp = 80)
@Composable
private fun CalcEqualsButtonPreview() {
    CalcCraftTheme {
        CalcButton(
            symbol = "=",
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = {}
        )
    }
}
