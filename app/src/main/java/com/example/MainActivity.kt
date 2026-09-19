package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.CallOraApp
import com.example.viewmodel.CallOraViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: CallOraViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    handleIntent(intent)
    setContent {
      CallOraApp(viewModel = viewModel)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    val uri: Uri? = intent?.data
    if (uri != null && uri.scheme == "callora" && uri.host == "auth") {
      viewModel.handleAuthDeepLink(uri)
    }
  }
}

