package com.calccraft.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calccraft.state.CalculatorIntent
import com.calccraft.state.CalculatorState
import com.calccraft.ui.components.CalcButton
import com.calccraft.ui.components.CalcButtonType
import com.calccraft.ui.components.DisplayPanel
import com.calccraft.ui.theme.CalcCraftTheme

// A data class to represent a button on the keypad
private data class KeypadButton(
    val text: String,
    val type: CalcButtonType,
    val intent: CalculatorIntent
)

// Define the keypad layout
private val keypadRows = listOf(
    listOf(
        KeypadButton("C", CalcButtonType.Action, CalculatorIntent.Clear),
        KeypadButton("()", CalcButtonType.Operator, CalculatorIntent.Append("(")),
        KeypadButton("%", CalcButtonType.Operator, CalculatorIntent.Append("%")),
        KeypadButton("÷", CalcButtonType.Operator, CalculatorIntent.Append("/"))
    ),
    listOf(
        KeypadButton("7", CalcButtonType.Number, CalculatorIntent.Append("7")),
        KeypadButton("8", CalcButtonType.Number, CalculatorIntent.Append("8")),
        KeypadButton("9", CalcButtonType.Number, CalculatorIntent.Append("9")),
        KeypadButton("×", CalcButtonType.Operator, CalculatorIntent.Append("*"))
    ),
    listOf(
        KeypadButton("4", CalcButtonType.Number, CalculatorIntent.Append("4")),
        KeypadButton("5", CalcButtonType.Number, CalculatorIntent.Append("5")),
        KeypadButton("6", CalcButtonType.Number, CalculatorIntent.Append("6")),
        KeypadButton("-", CalcButtonType.Operator, CalculatorIntent.Append("-"))
    ),
    listOf(
        KeypadButton("1", CalcButtonType.Number, CalculatorIntent.Append("1")),
        KeypadButton("2", CalcButtonType.Number, CalculatorIntent.Append("2")),
        KeypadButton("3", CalcButtonType.Number, CalculatorIntent.Append("3")),
        KeypadButton("+", CalcButtonType.Operator, CalculatorIntent.Append("+"))
    ),
    listOf(
        KeypadButton("⌫", CalcButtonType.Action, CalculatorIntent.Delete),
        KeypadButton("0", CalcButtonType.Number, CalculatorIntent.Append("0")),
        KeypadButton(".", CalcButtonType.Number, CalculatorIntent.Append(".")),
        KeypadButton("=", CalcButtonType.Equals, CalculatorIntent.Evaluate)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    state: CalculatorState,
    onIntent: (CalculatorIntent) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "CalcCraft") },
                actions = {
                    IconButton(onClick = { onIntent(CalculatorIntent.SwitchDestination("history")) }) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .navigationBarsPadding()
        ) {
            DisplayPanel(
                modifier = Modifier.weight(1f),
                currentExpression = state.currentExpression,
                lastResult = state.lastResult,
                errorMessage = state.errorMessage
            )

            Keypad(
                onIntent = onIntent,
                modifier = Modifier
                    .weight(1.8f) // Give keypad more space
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun Keypad(
    onIntent: (CalculatorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { button ->
                    CalcButton(
                        text = button.text,
                        onClick = { onIntent(button.intent) },
                        type = button.type,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun CalculatorScreenPreview() {
    CalcCraftTheme(darkTheme = true) {
        CalculatorScreen(
            state = CalculatorState(
                currentExpression = "3.14 * (2 + 2)",
                lastResult = "= 12.56"
            ),
            onIntent = {}
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun CalculatorScreenErrorPreview() {
    CalcCraftTheme(darkTheme = true) {
        CalculatorScreen(
            state = CalculatorState(
                currentExpression = "1/0",
                lastResult = "= 12.56", // Stale result
                errorMessage = "Error: Division by zero"
            ),
            onIntent = {}
        )
    }
}
