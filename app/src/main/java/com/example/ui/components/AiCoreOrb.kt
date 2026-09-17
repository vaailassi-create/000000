package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AnimationIntensity
import com.example.data.model.AssistantState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SynthMagenta
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class OrbParticle(
    val baseAngle: Float,
    val distanceMultiplier: Float,
    val speed: Float,
    val sizeDp: Float
)

@Composable
fun AiCoreOrb(
    state: AssistantState,
    amplitude: Float,
    accentColor: Color,
    animationIntensity: AnimationIntensity,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfinite")

    // Slow breathing glow
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breathing"
    )

    // Ring 1 rotation (clockwise)
    val ring1Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == AssistantState.THINKING) 3000 else 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring1"
    )

    // Ring 2 rotation (counter-clockwise)
    val ring2Rotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == AssistantState.THINKING) 2200 else 16000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring2"
    )

    // Core expansion animated state
    val targetScale = when (state) {
        AssistantState.IDLE -> 1.0f * breathingPulse
        AssistantState.LISTENING -> 1.25f + (amplitude * 0.45f)
        AssistantState.THINKING -> 1.15f * breathingPulse
        AssistantState.SPEAKING -> 1.20f + (amplitude * 0.40f)
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "Scale"
    )

    // State Accent Colors
    val currentAccent = when (state) {
        AssistantState.IDLE -> accentColor
        AssistantState.LISTENING -> ElectricCyan
        AssistantState.THINKING -> NeonViolet
        AssistantState.SPEAKING -> SynthMagenta
    }

    // Fixed pre-allocated lightweight particles
    val particles = remember {
        listOf(
            OrbParticle(0f, 1.15f, 1.0f, 3.5f),
            OrbParticle(45f, 1.35f, -0.8f, 2.5f),
            OrbParticle(90f, 1.22f, 1.2f, 4f),
            OrbParticle(140f, 1.40f, -1.1f, 3f),
            OrbParticle(200f, 1.18f, 0.9f, 2.5f),
            OrbParticle(260f, 1.32f, -0.7f, 3.5f),
            OrbParticle(310f, 1.28f, 1.3f, 4f)
        )
    }

    Box(
        modifier = modifier
            .testTag("ai_core_orb")
            .size(170.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTap()
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    },
                    onPress = {
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(170.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 4.2f
            val coreRadius = baseRadius * animatedScale

            // 1. Soft Outer Neon Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        currentAccent.copy(alpha = 0.55f),
                        currentAccent.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 2.2f
                ),
                radius = coreRadius * 2.2f,
                center = center
            )

            // 2. Rotating HUD Ring 1 (Dashed Outer)
            val ring1Radius = coreRadius * 1.55f
            val dashPattern = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), ring1Rotation)
            drawCircle(
                color = currentAccent.copy(alpha = 0.55f),
                radius = ring1Radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx(), pathEffect = dashPattern)
            )

            // 3. Rotating HUD Ring 2 (Concentric Inner)
            val ring2Radius = coreRadius * 1.25f
            val dashPattern2 = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), ring2Rotation)
            drawCircle(
                color = currentAccent.copy(alpha = 0.40f),
                radius = ring2Radius,
                center = center,
                style = Stroke(width = 1.dp.toPx(), pathEffect = dashPattern2)
            )

            // 4. Processing Arcs for Thinking state
            if (state == AssistantState.THINKING) {
                val arcRadius = coreRadius * 1.75f
                drawArc(
                    color = NeonViolet,
                    startAngle = ring1Rotation,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawArc(
                    color = ElectricCyan,
                    startAngle = ring2Rotation + 180f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // 5. Dynamic circular waveform reacting to audio
            if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) {
                val wavePoints = 28
                val waveRadius = coreRadius * 1.35f
                for (i in 0 until wavePoints) {
                    val angle = (i * (360f / wavePoints)) * (PI.toFloat() / 180f)
                    val waveAmp = amplitude * 18.dp.toPx() * (sin((i * 3 + ring1Rotation * 0.1f).toDouble()).toFloat())
                    val x1 = center.x + waveRadius * cos(angle)
                    val y1 = center.y + waveRadius * sin(angle)
                    val x2 = center.x + (waveRadius + waveAmp.coerceAtLeast(2f)) * cos(angle)
                    val y2 = center.y + (waveRadius + waveAmp.coerceAtLeast(2f)) * sin(angle)

                    drawLine(
                        color = currentAccent.copy(alpha = 0.85f),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // 6. Solid Glowing Central Core Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        currentAccent,
                        currentAccent.copy(alpha = 0.75f)
                    ),
                    center = Offset(center.x - coreRadius * 0.2f, center.y - coreRadius * 0.2f),
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // Inner X symbol on Core
            val xHalf = coreRadius * 0.40f
            val xColor = Color(0xDD04060A)
            val strokeW = 3.dp.toPx()
            drawLine(xColor, Offset(center.x - xHalf, center.y - xHalf), Offset(center.x + xHalf, center.y + xHalf), strokeW)
            drawLine(xColor, Offset(center.x + xHalf, center.y - xHalf), Offset(center.x - xHalf, center.y + xHalf), strokeW)

            // 7. Floating Holographic Particles
            if (animationIntensity != AnimationIntensity.LOW) {
                for (p in particles) {
                    val particleAngle = ((p.baseAngle + ring1Rotation * p.speed) % 360f) * (PI.toFloat() / 180f)
                    val pDist = coreRadius * p.distanceMultiplier
                    val px = center.x + pDist * cos(particleAngle)
                    val py = center.y + pDist * sin(particleAngle)

                    drawCircle(
                        color = currentAccent.copy(alpha = 0.75f),
                        radius = p.sizeDp.dp.toPx() / 2f,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
