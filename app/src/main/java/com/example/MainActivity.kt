package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HyperTubeStudioScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HyperTubeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HyperTubeViewModel by viewModels {
        HyperTubeViewModel.provideFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingYouTubeIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(themeMode = uiState.preferences.themeMode) {
                HyperTubeStudioScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingYouTubeIntent(intent)
    }

    private fun handleIncomingYouTubeIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        viewModel.submitUrlOrText(sharedText)
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                val dataUrl = intent.dataString
                if (!dataUrl.isNullOrBlank()) {
                    viewModel.submitUrlOrText(dataUrl)
                }
            }
        }
    }
}
