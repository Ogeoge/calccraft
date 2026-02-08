package com.calccraft.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calccraft.R
import com.calccraft.model.HistoryEntry
import com.calccraft.ui.theme.CalcCraftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: List<HistoryEntry>,
    onClearHistory: () -> Unit,
    onExportHistory: () -> String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Scroll to the bottom when the history list changes
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_navigate_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onClearHistory,
                        enabled = history.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.cd_clear_history)
                        )
                    }
                    IconButton(
                        onClick = {
                            val historyText = onExportHistory()
                            shareHistory(context, historyText)
                        },
                        enabled = history.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(id = R.string.cd_export_history)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        if (history.isEmpty()) {
            EmptyHistoryView(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
                reverseLayout = true
            ) {
                items(history, key = { it.id }) { entry ->
                    HistoryRow(entry = entry)
                    if (history.first() != entry) {
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.no_history_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = entry.expression,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
        Text(
            text = entry.resultText,
            style = MaterialTheme.typography.headlineSmall,
            color = if (entry.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun shareHistory(context: Context, historyText: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, historyText)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.export_history_title))
    context.startActivity(shareIntent)
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    CalcCraftTheme {
        HistoryScreen(
            history = listOf(
                HistoryEntry(1, "2+2", "4", false, System.currentTimeMillis()),
                HistoryEntry(2, "10/0", "Error: Division by zero", true, System.currentTimeMillis()),
                HistoryEntry(3, "sqrt(16)*2", "8", false, System.currentTimeMillis()),
                HistoryEntry(4, "50%", "0.5", false, System.currentTimeMillis()),
            ),
            onClearHistory = {},
            onExportHistory = { "" },
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenEmptyPreview() {
    CalcCraftTheme {
        HistoryScreen(
            history = emptyList(),
            onClearHistory = {},
            onExportHistory = { "" },
            onNavigateBack = {}
        )
    }
}
