package com.calccraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.core.view.WindowCompat
import com.calccraft.state.CalculatorViewModel
import com.calccraft.ui.keyboard.HardwareKeyMapper
import com.calccraft.ui.screens.CalculatorScreen
import com.calccraft.ui.screens.HistoryScreen
import com.calccraft.ui.theme.CalcCraftTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CalcCraftTheme {
                val state by viewModel.state.collectAsState()

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .onKeyEvent {
                            if (it.type == KeyEventType.KeyUp) {
                                HardwareKeyMapper
                                    .map(it)
                                    ?.let(viewModel::onIntent)
                            }
                            // Return true if we handled the event, to prevent system processing
                            // for keys like Enter or Backspace. We handle all relevant keys.
                            it.key != Key.Unknown
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(targetState = state.destination, label = "screen_navigation") {
                        destination ->
                        when (destination) {
                            "calculator" -> CalculatorScreen(
                                state = state,
                                onIntent = viewModel::onIntent
                            )
                            "history" -> HistoryScreen(
                                state = state,
                                onIntent = viewModel::onIntent
                            )
                        }
                    }
                }
            }
        }
    }
}
