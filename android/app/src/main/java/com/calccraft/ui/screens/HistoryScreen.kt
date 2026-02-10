package com.calccraft.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calccraft.domain.model.EvalResult
import com.calccraft.domain.model.HistoryEntry
import com.calccraft.state.CalculatorIntent
import com.calccraft.state.CalculatorState
import com.calccraft.ui.theme.CalcCraftTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: CalculatorState,
    onIntent: (CalculatorIntent) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "History") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(CalculatorIntent.SwitchDestination("calculator")) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Calculator")
                    }
                },
                actions = {
                    if (state.history.isNotEmpty()) {
                        IconButton(onClick = { onIntent(CalculatorIntent.ClearHistory) }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear History")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (state.history.isEmpty()) {
            EmptyHistoryView(modifier = Modifier.padding(paddingValues))
        } else {
            HistoryListView(
                history = state.history,
                onIntent = onIntent,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EmptyHistoryView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No history yet. Start calculating!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistoryListView(
    history: List<HistoryEntry>,
    onIntent: (CalculatorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(history, key = { it.id }) { entry ->
            HistoryItem(entry = entry, onIntent = onIntent)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun HistoryItem(
    entry: HistoryEntry,
    onIntent: (CalculatorIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIntent(CalculatorIntent.UseHistoryEntry(entry.id)) }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = entry.expression,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )

        val resultColor = when (entry.result) {
            is EvalResult.Error -> MaterialTheme.colorScheme.error
            is EvalResult.Success -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        val resultText = when (val result = entry.result) {
            is EvalResult.Success -> "= ${result.formatted}"
            is EvalResult.Error -> result.message ?: "Unknown Error"
        }

        Text(
            text = resultText,
            style = MaterialTheme.typography.bodyLarge,
            color = resultColor,
            textAlign = TextAlign.End
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun HistoryScreenPreview() {
    val sampleHistory = listOf(
        HistoryEntry(
            id = UUID.randomUUID().toString(),
            timestampMs = System.currentTimeMillis(),
            expression = "2+2*2",
            result = EvalResult.Success(value = 6.0, formatted = "6")
        ),
        HistoryEntry(
            id = UUID.randomUUID().toString(),
            timestampMs = System.currentTimeMillis() - 1000,
            expression = "sqrt(-1)",
            result = EvalResult.Error(errorKind = "DomainError", message = "Domain error")
        ),
        HistoryEntry(
            id = UUID.randomUUID().toString(),
            timestampMs = System.currentTimeMillis() - 2000,
            expression = "1/0",
            result = EvalResult.Error(errorKind = "DivideByZero", message = "Division by zero")
        ),
        HistoryEntry(
            id = UUID.randomUUID().toString(),
            timestampMs = System.currentTimeMillis() - 3000,
            expression = "3.14159 * 10",
            result = EvalResult.Success(value = 31.4159, formatted = "31.4159")
        )
    )

    CalcCraftTheme(darkTheme = true) {
        HistoryScreen(
            state = CalculatorState(history = sampleHistory, destination = "history"),
            onIntent = {}
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun HistoryScreenEmptyPreview() {
    CalcCraftTheme(darkTheme = true) {
        HistoryScreen(
            state = CalculatorState(history = emptyList(), destination = "history"),
            onIntent = {}
        )
    }
}
