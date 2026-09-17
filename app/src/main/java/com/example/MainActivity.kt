package com.example

import android.content.Intent
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AssistantState
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AssistantXTheme
import com.example.ui.theme.CyberBlack
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.XAssistantViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: XAssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleVoiceTriggerIntent(intent)

        setContent {
            AssistantXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberBlack
                ) {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    // Handle Back button
                    BackHandler {
                        when (uiState.currentScreen) {
                            AppScreen.SETTINGS -> viewModel.navigateTo(AppScreen.HOME)
                            AppScreen.ONBOARDING -> viewModel.navigateTo(AppScreen.HOME)
                            AppScreen.HOME -> {
                                if (uiState.state != AssistantState.IDLE) {
                                    viewModel.cancelInteraction()
                                } else {
                                    finish()
                                }
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = uiState.currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.HOME -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onOrbTap = {
                                        if (uiState.state == AssistantState.LISTENING) {
                                            viewModel.stopListening()
                                        } else if (uiState.state == AssistantState.SPEAKING) {
                                            viewModel.cancelInteraction()
                                        } else {
                                            viewModel.startListening()
                                        }
                                    },
                                    onOrbLongPress = {
                                        viewModel.startListening()
                                    },
                                    onOrbRelease = {
                                        if (uiState.state == AssistantState.LISTENING) {
                                            viewModel.stopListening()
                                        }
                                    },
                                    onOpenSettings = {
                                        viewModel.navigateTo(AppScreen.SETTINGS)
                                    },
                                    onSwipeDown = {
                                        if (uiState.state != AssistantState.IDLE) {
                                            viewModel.cancelInteraction()
                                        }
                                    }
                                )
                            }
                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(AppScreen.HOME) },
                                    onRequestPermissions = { viewModel.navigateTo(AppScreen.ONBOARDING) }
                                )
                            }
                            AppScreen.ONBOARDING -> {
                                OnboardingScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onComplete = { viewModel.navigateTo(AppScreen.HOME) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleVoiceTriggerIntent(intent)
    }

    private fun handleVoiceTriggerIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("TRIGGER_VOICE", false) == true) {
            viewModel.startListening()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
    }
}
