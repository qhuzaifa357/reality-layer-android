package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.AroundMeScreen
import com.example.ui.AskAiScreen
import com.example.ui.HistoryScreen
import com.example.ui.HomeScreen
import com.example.ui.MainViewModel
import com.example.ui.ScanScreen
import com.example.ui.ScreenRoute
import com.example.ui.theme.RealityLayerTheme

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RealityLayerTheme {
        val currentRoute by viewModel.currentRoute.collectAsState()

        // Handle back button to return to home if on secondary screen
        BackHandler(enabled = currentRoute != ScreenRoute.HOME) {
          viewModel.navigateTo(ScreenRoute.HOME)
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AnimatedContent(
            targetState = currentRoute,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(innerPadding),
            label = "screen_transition"
          ) { route ->
            when (route) {
              ScreenRoute.HOME -> HomeScreen(viewModel = viewModel)
              ScreenRoute.SCAN -> ScanScreen(viewModel = viewModel)
              ScreenRoute.ASK_AI -> AskAiScreen(viewModel = viewModel)
              ScreenRoute.AROUND_ME -> AroundMeScreen(viewModel = viewModel)
              ScreenRoute.HISTORY -> HistoryScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Reality Layer: $name", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  RealityLayerTheme { Greeting("AI Assistant") }
}

