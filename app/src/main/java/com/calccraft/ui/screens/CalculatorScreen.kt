package com.calccraft.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calccraft.R
import com.calccraft.state.CalcIntent
import com.calccraft.state.CalcState
import com.calccraft.ui.components.CalcButton
import com.calccraft.ui.components.Display

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    state: CalcState,
    onIntent: (CalcIntent) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.cd_history_button)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Display(
                text = state.displayValue,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Keypad(onIntent = onIntent)
        }
    }
}

@Composable
private fun Keypad(onIntent: (CalcIntent) -> Unit, modifier: Modifier = Modifier) {
    val buttonSpacing = 8.dp
    val buttonLayout = listOf(
        listOf("sin", "cos", "tan", "log"),
        listOf("√", "^", "(", ")"),
        listOf("C", "%", "⌫", "/"),
        listOf("7", "8", "9", "*"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(buttonSpacing),
    ) {
        buttonLayout.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                row.forEach { button ->
                    val weight = if (button == "0") 2f else 1f

                    val intent = when (button) {
                        "C" -> CalcIntent.Clear
                        "⌫" -> CalcIntent.Delete
                        "=" -> CalcIntent.Evaluate
                        "sin", "cos", "tan", "log", "√" -> CalcIntent.AppendFunction(button)
                        else -> CalcIntent.AppendChar(button)
                    }

                    CalcButton(
                        modifier = Modifier.weight(weight),
                        onClick = { onIntent(intent) }
                    ) {
                        if (button == "⌫") {
                            Icon(
                                Icons.Outlined.Backspace,
                                contentDescription = stringResource(R.string.cd_delete_button),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(text = button, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
    }
}
