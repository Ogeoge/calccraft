package com.calccraft.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calccraft.ui.theme.*

enum class CalcButtonType {
    Number,
    Operator,
    Action,
    Equals
}

@Composable
fun CalcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: CalcButtonType = CalcButtonType.Number,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "scale_animation")

    val (backgroundColor, contentColor) = getButtonColors(type = type)

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .padding(6.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Using scale animation instead of ripple
                enabled = enabled,
                onClick = onClick
            ),
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun getButtonColors(type: CalcButtonType): Pair<Color, Color> {
    return if (isSystemInDarkTheme()) {
        when (type) {
            CalcButtonType.Number -> CalcButtonNumberBg to CalcButtonNumberText
            CalcButtonType.Operator -> CalcButtonOperatorBg to CalcButtonOperatorText
            CalcButtonType.Action -> CalcButtonActionBg to CalcButtonActionText
            CalcButtonType.Equals -> CalcButtonEqualsBg to CalcButtonEqualsText
        }
    } else {
        // Light theme colors
        when (type) {
            CalcButtonType.Number -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
            CalcButtonType.Operator -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            CalcButtonType.Action -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
            CalcButtonType.Equals -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        }
    }
}


@Preview(showBackground = true, widthDp = 100, heightDp = 100)
@Composable
fun CalcButtonPreviewLight() {
    CalcCraftTheme(darkTheme = false) {
        CalcButton(text = "7", onClick = {})
    }
}

@Preview(showBackground = true, widthDp = 100, heightDp = 100)
@Composable
fun CalcButtonPreviewDark() {
    CalcCraftTheme(darkTheme = true) {
        CalcButton(text = "7", onClick = {})
    }
}

@Preview(showBackground = true, widthDp = 100, heightDp = 100)
@Composable
fun CalcButtonOperatorPreview() {
    CalcCraftTheme(darkTheme = true) {
        CalcButton(text = "+", onClick = {}, type = CalcButtonType.Operator)
    }
}

@Preview(showBackground = true, widthDp = 100, heightDp = 100)
@Composable
fun CalcButtonEqualsPreview() {
    CalcCraftTheme(darkTheme = true) {
        CalcButton(text = "=", onClick = {}, type = CalcButtonType.Equals)
    }
}
