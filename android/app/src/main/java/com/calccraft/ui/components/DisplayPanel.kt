package com.calccraft.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calccraft.ui.theme.CalcCraftTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DisplayPanel(
    modifier: Modifier = Modifier,
    currentExpression: String,
    lastResult: String?,
    errorMessage: String?
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Animated secondary display for result or error
        AnimatedContent(
            targetState = errorMessage ?: lastResult,
            transitionSpec = {
                (slideInVertically(animationSpec = tween(200)) { height -> height } + fadeIn(animationSpec = tween(200)))
                    .togetherWith(slideOutVertically(animationSpec = tween(200)) { height -> height } + fadeOut(animationSpec = tween(200)))
                    .using(SizeTransform(clip = false))
            },
            label = "result_error_transition"
        ) { text ->
            Text(
                text = text ?: "",
                color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main display for current expression
        val scrollState = rememberScrollState()
        LaunchedEffect(currentExpression) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        Text(
            text = currentExpression.ifEmpty { "0" },
            modifier = Modifier.horizontalScroll(scrollState),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF191C20)
@Composable
fun DisplayPanelPreviewInitial() {
    CalcCraftTheme(darkTheme = true) {
        DisplayPanel(
            currentExpression = "",
            lastResult = null,
            errorMessage = null
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF191C20)
@Composable
fun DisplayPanelWithResultPreview() {
    CalcCraftTheme(darkTheme = true) {
        DisplayPanel(
            currentExpression = "12+sqrt(9)",
            lastResult = "= 15",
            errorMessage = null
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF191C20)
@Composable
fun DisplayPanelWithErrorPreview() {
    CalcCraftTheme(darkTheme = true) {
        DisplayPanel(
            currentExpression = "1/0",
            lastResult = "= 15", // Stale result
            errorMessage = "Error: Division by zero"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
fun DisplayPanelPreviewLight() {
    CalcCraftTheme(darkTheme = false) {
        DisplayPanel(
            currentExpression = "3.14 * 2",
            lastResult = "= 6.28",
            errorMessage = null
        )
    }
}
