package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AssistantState
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SynthMagenta
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimeCompanionView(
    state: AssistantState,
    amplitude: Float,
    accentColor: Color,
    animated: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CompanionAnim")

    // Breathing float animation
    val breathingOffsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingY"
    )

    // Holographic aura rotation
    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraRotation"
    )

    // Head tilt angle for Listening/Thinking states
    val headTilt = when (state) {
        AssistantState.LISTENING -> -2.5f
        AssistantState.THINKING -> 3f
        AssistantState.SPEAKING -> sin(breathingOffsetY * 0.4f) * 1.5f
        AssistantState.IDLE -> 0f
    }

    // Interactive scale reaction based on audio
    val audioPulse = 1f + (amplitude * 0.08f)

    // Periodic blinking state
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        while (true) {
            delay(Random.nextLong(3000, 6500))
            isBlinking = true
            delay(140)
            isBlinking = false
        }
    }

    // Dynamic state aura color
    val stateAuraColor = when (state) {
        AssistantState.IDLE -> accentColor
        AssistantState.LISTENING -> ElectricCyan
        AssistantState.THINKING -> NeonViolet
        AssistantState.SPEAKING -> SynthMagenta
    }

    Box(
        modifier = modifier
            .testTag("anime_companion_container")
            .size(width = 280.dp, height = 340.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Holographic Aura Rings behind companion
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (animated) auraRotation else 0f)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) * 0.92f

            // Outer segmented cyber HUD circle
            drawCircle(
                color = stateAuraColor.copy(alpha = 0.22f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Segmented tick marks
            val tickCount = 24
            for (i in 0 until tickCount) {
                val angle = (i * (360f / tickCount)) * (PI.toFloat() / 180f)
                val start = Offset(
                    x = center.x + (radius - 6.dp.toPx()) * cos(angle),
                    y = center.y + (radius - 6.dp.toPx()) * sin(angle)
                )
                val end = Offset(
                    x = center.x + radius * cos(angle),
                    y = center.y + radius * sin(angle)
                )
                val tickColor = if (i % 4 == 0) stateAuraColor.copy(alpha = 0.8f) else stateAuraColor.copy(alpha = 0.3f)
                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Audio amplitude wave rings around character
            if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) {
                val waveRadius = radius * (0.85f + amplitude * 0.18f)
                drawCircle(
                    color = stateAuraColor.copy(alpha = (0.4f * amplitude).coerceIn(0.1f, 0.6f)),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 2. Character Portrait Box with Breathing & Head movement
        Box(
            modifier = Modifier
                .size(width = 230.dp, height = 290.dp)
                .offset {
                    IntOffset(
                        x = 0,
                        y = if (animated) breathingOffsetY.roundToInt() else 0
                    )
                }
                .rotate(if (animated) headTilt else 0f)
                .scale(if (animated) audioPulse else 1f)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            stateAuraColor.copy(alpha = 0.25f),
                            CyberBlack.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            stateAuraColor.copy(alpha = 0.8f),
                            stateAuraColor.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Main Anime Companion Image
            Image(
                painter = painterResource(id = R.drawable.img_anime_companion),
                contentDescription = "X Anime AI Companion",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("anime_companion_image")
            )

            // Holographic scanline overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 8.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = stateAuraColor.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += step
                }
            }

            // Interactive Blinking Eye Overlay Effect
            if (isBlinking) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            // Subtly close eyes area during blink
                            val eyeY = size.height * 0.38f
                            val eyeWidth = size.width * 0.12f
                            val leftEyeX = size.width * 0.40f
                            val rightEyeX = size.width * 0.58f

                            drawOval(
                                color = Color(0xCC1A162B),
                                topLeft = Offset(leftEyeX - eyeWidth / 2f, eyeY - 4.dp.toPx()),
                                size = Size(eyeWidth, 8.dp.toPx())
                            )
                            drawOval(
                                color = Color(0xCC1A162B),
                                topLeft = Offset(rightEyeX - eyeWidth / 2f, eyeY - 4.dp.toPx()),
                                size = Size(eyeWidth, 8.dp.toPx())
                            )
                        }
                )
            }

            // Interactive Lip-sync Speaking Overlay Effect
            if (state == AssistantState.SPEAKING && amplitude > 0.15f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val mouthY = size.height * 0.52f
                            val mouthX = size.width * 0.49f
                            val mouthOpenH = (amplitude * 10.dp.toPx()).coerceIn(3.dp.toPx(), 9.dp.toPx())
                            val mouthW = 12.dp.toPx()

                            // Subtle lip opening animation overlay
                            drawOval(
                                color = Color(0xDD8B2B4A),
                                topLeft = Offset(mouthX - mouthW / 2f, mouthY - mouthOpenH / 2f),
                                size = Size(mouthW, mouthOpenH)
                            )
                        }
                )
            }

            // Corner Cyber HUD Brackets
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bracketLen = 16.dp.toPx()
                val bracketColor = stateAuraColor.copy(alpha = 0.7f)
                val strokeW = 1.5.dp.toPx()

                // Top-Left
                drawLine(bracketColor, Offset(8.dp.toPx(), 8.dp.toPx()), Offset(8.dp.toPx() + bracketLen, 8.dp.toPx()), strokeW)
                drawLine(bracketColor, Offset(8.dp.toPx(), 8.dp.toPx()), Offset(8.dp.toPx(), 8.dp.toPx() + bracketLen), strokeW)

                // Top-Right
                drawLine(bracketColor, Offset(size.width - 8.dp.toPx(), 8.dp.toPx()), Offset(size.width - 8.dp.toPx() - bracketLen, 8.dp.toPx()), strokeW)
                drawLine(bracketColor, Offset(size.width - 8.dp.toPx(), 8.dp.toPx()), Offset(size.width - 8.dp.toPx(), 8.dp.toPx() + bracketLen), strokeW)

                // Bottom-Left
                drawLine(bracketColor, Offset(8.dp.toPx(), size.height - 8.dp.toPx()), Offset(8.dp.toPx() + bracketLen, size.height - 8.dp.toPx()), strokeW)
                drawLine(bracketColor, Offset(8.dp.toPx(), size.height - 8.dp.toPx()), Offset(8.dp.toPx(), size.height - 8.dp.toPx() - bracketLen), strokeW)

                // Bottom-Right
                drawLine(bracketColor, Offset(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()), Offset(size.width - 8.dp.toPx() - bracketLen, size.height - 8.dp.toPx()), strokeW)
                drawLine(bracketColor, Offset(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()), Offset(size.width - 8.dp.toPx(), size.height - 8.dp.toPx() - bracketLen), strokeW)
            }
        }
    }
}
