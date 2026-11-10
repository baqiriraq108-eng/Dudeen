package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request maximum supported refresh rate (e.g. 144Hz on Xiaomi Mi 14T Pro) for 144 FPS rendering
        try {
            @Suppress("DEPRECATION")
            window.attributes = window.attributes.apply {
                preferredRefreshRate = 144f
            }
        } catch (_: Exception) {
            // Ignore if display mode is locked by OS
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (uiState.themeMode.lowercase()) {
                "light", "hell" -> false
                "dark", "dunkel" -> true
                else -> isSystemDark
            }
            MyApplicationTheme(
                selectedTheme = uiState.selectedTheme,
                darkTheme = isDark
            ) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
