package com.calccraft.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calccraft.ui.theme.CalcCraftTheme

@Composable
fun Display(
    displayValue: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp) // Give it a fixed, generous height
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        AnimatedContent(
            targetState = displayValue,
            transitionSpec = {
                if (targetState.length > initialState.length) {
                    // Entering a longer string (e.g. typing)
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                } else {
                    // Entering a shorter string (e.g. deleting, result)
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                }.using(
                    SizeTransform(clip = false)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = "DisplayValueAnimation"
        ) { targetText ->
            Text(
                text = targetText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DisplayPreview() {
    CalcCraftTheme {
        Surface {
            Display(displayValue = "123,456.789")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DisplayErrorPreview() {
    CalcCraftTheme {
        Surface {
            Display(displayValue = "Error: Division by zero")
        }
    }
}
