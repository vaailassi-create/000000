package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.ui.components.ActiveActionBanner
import com.example.ui.components.AiCoreOrb
import com.example.ui.components.AnimeCompanionView
import com.example.ui.components.AudioWaveformIndicator
import com.example.ui.components.StatusBadge
import com.example.ui.components.TopCyberBar
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNearBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AssistantUiState

@Composable
fun HomeScreen(
    uiState: AssistantUiState,
    onOrbTap: () -> Unit,
    onOrbLongPress: () -> Unit,
    onOrbRelease: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = uiState.themeAccent.primaryColor
    val activeAmplitude = if (uiState.state == AssistantState.SPEAKING) uiState.outputAmplitude else uiState.micAmplitude

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        dragOffset += delta
        if (dragOffset < -90f) {
            // Swiped up -> open settings
            dragOffset = 0f
            onOpenSettings()
        } else if (dragOffset > 100f) {
            // Swiped down -> minimize / cancel
            dragOffset = 0f
            onSwipeDown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_container")
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberBlack,
                        CyberNearBlack,
                        CyberBlack
                    )
                )
            )
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { dragOffset = 0f }
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Cyber HUD Bar
            TopCyberBar(
                state = uiState.state,
                accentColor = accentColor,
                onSettingsClick = onOpenSettings
            )

            // 2. Active Action Pill (When tools execute)
            ActiveActionBanner(
                action = uiState.activeAction,
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.weight(0.15f))

            // 3. Anime AI Companion (Main Visual Character)
            AnimeCompanionView(
                state = uiState.state,
                amplitude = activeAmplitude,
                accentColor = accentColor,
                animated = uiState.companionAnimationEnabled,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Large Glowing AI Core / Orb
            AiCoreOrb(
                state = uiState.state,
                amplitude = activeAmplitude,
                accentColor = accentColor,
                animationIntensity = uiState.animationIntensity,
                onTap = onOrbTap,
                onLongPress = onOrbLongPress,
                onRelease = onOrbRelease
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Audio Waveform Indicators
            AudioWaveformIndicator(
                amplitude = activeAmplitude,
                accentColor = accentColor,
                barCount = 11
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Status Badge
            StatusBadge(
                state = uiState.state,
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 7. Minimal Status / Spoken text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.state == AssistantState.IDLE) {
                    Text(
                        text = "\"Say X to begin\"",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                } else if (uiState.state == AssistantState.LISTENING) {
                    Text(
                        text = if (uiState.recognizedText.isNotBlank()) "\"${uiState.recognizedText}\"" else "Listening for speech...",
                        color = accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                } else if (uiState.state == AssistantState.THINKING) {
                    Text(
                        text = "Synthesizing neural command...",
                        color = TextWhite.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                } else if (uiState.state == AssistantState.SPEAKING) {
                    Text(
                        text = uiState.spokenResponse.take(120),
                        color = TextWhite,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }

            // Bottom swipe hint
            Text(
                text = "▲ Swipe up for Settings • Tap orb to speak",
                color = TextMuted.copy(alpha = 0.45f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}
