package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantAction
import com.example.data.model.AssistantState
import com.example.ui.theme.CyberGlass
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HologramGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SynthMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun TopCyberBar(
    state: AssistantState,
    accentColor: Color,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Branding
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("brand_header")
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        when (state) {
                            AssistantState.IDLE -> HologramGreen
                            AssistantState.LISTENING -> ElectricCyan
                            AssistantState.THINKING -> NeonViolet
                            AssistantState.SPEAKING -> SynthMagenta
                        }
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "X",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "AI",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }

        // Right Settings Gear
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CyberGlass)
                .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape)
                .testTag("settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Open Settings",
                tint = accentColor
            )
        }
    }
}

@Composable
fun AudioWaveformIndicator(
    amplitude: Float,
    accentColor: Color,
    barCount: Int = 9,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(26.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val factor = 1f - kotlin.math.abs((i - (barCount / 2)) / (barCount / 2f))
            val barHeight = (4.dp + (22.dp * (amplitude * factor).coerceIn(0f, 1f)))

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun StatusBadge(
    state: AssistantState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val stateColor = when (state) {
        AssistantState.IDLE -> accentColor
        AssistantState.LISTENING -> ElectricCyan
        AssistantState.THINKING -> NeonViolet
        AssistantState.SPEAKING -> SynthMagenta
    }

    Box(
        modifier = modifier
            .testTag("status_badge")
            .clip(RoundedCornerShape(16.dp))
            .background(CyberGlass)
            .border(1.dp, stateColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (state) {
                    AssistantState.IDLE -> Icons.Default.Bolt
                    AssistantState.LISTENING -> Icons.Default.Mic
                    AssistantState.THINKING -> Icons.Default.Bolt
                    AssistantState.SPEAKING -> Icons.AutoMirrored.Filled.VolumeUp
                },
                contentDescription = null,
                tint = stateColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = state.displayName,
                color = stateColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun ActiveActionBanner(
    action: AssistantAction?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = action != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        if (action != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberGlass)
                    .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("active_action_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = action.title,
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = action.detail,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
