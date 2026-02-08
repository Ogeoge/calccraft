package com.calccraft

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.calccraft.navigation.AppNav
import com.calccraft.state.CalcViewModel
import com.calccraft.ui.keyboard.HardwareKeyMapper
import com.calccraft.ui.theme.CalcCraftTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalcCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(viewModel = viewModel)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            HardwareKeyMapper.map(it)?.let {
                intent -> viewModel.onIntent(intent)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
