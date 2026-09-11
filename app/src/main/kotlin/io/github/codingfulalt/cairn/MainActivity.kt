package io.github.codingfulalt.cairn

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnTheme
import io.github.codingfulalt.cairn.core.model.ThemeMode
import io.github.codingfulalt.cairn.ui.CairnApp
import io.github.codingfulalt.cairn.ui.MainUiState
import io.github.codingfulalt.cairn.ui.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = shouldUseDarkTheme(uiState)

            // the in-app theme can differ from the system one, keep the bar icons readable
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle =
                        SystemBarStyle.auto(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                        ) { darkTheme },
                    navigationBarStyle =
                        SystemBarStyle.auto(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                        ) { darkTheme },
                )
                onDispose {}
            }

            CairnTheme(darkTheme = darkTheme) {
                val state = uiState
                if (state is MainUiState.Ready) {
                    CairnApp(showOnboarding = !state.onboardingCompleted)
                }
            }
        }
    }
}

@Composable
private fun shouldUseDarkTheme(state: MainUiState): Boolean =
    when (state) {
        MainUiState.Loading -> isSystemInDarkTheme()
        is MainUiState.Ready ->
            when (state.themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
    }
