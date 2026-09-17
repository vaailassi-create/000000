package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimationIntensity
import com.example.data.model.ThemeAccent
import com.example.data.model.VoiceTone
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberDarkCharcoal
import com.example.ui.theme.CyberGlass
import com.example.ui.theme.CyberNearBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HologramGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AssistantUiState
import com.example.ui.viewmodel.XAssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: AssistantUiState,
    viewModel: XAssistantViewModel,
    onBack: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = uiState.themeAccent.primaryColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_container")
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberBlack, CyberNearBlack, CyberBlack)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "CORE SETTINGS",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = accent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Connection & Neural Engine Status Card
                item {
                    GlassPanel(title = "NEURAL ENGINE", accent = accent) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Gemini Live Link",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (uiState.isGeminiConnected) "Online • gemini-3.5-flash" else "Local Intent Fallback",
                                    color = if (uiState.isGeminiConnected) HologramGreen else Color(0xFFF59E0B),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (uiState.isGeminiConnected) HologramGreen.copy(alpha = 0.15f)
                                        else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    )
                                    .border(
                                        1.dp,
                                        if (uiState.isGeminiConnected) HologramGreen else Color(0xFFF59E0B),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (uiState.isGeminiConnected) "CONNECTED" else "LOCAL ONLY",
                                    color = if (uiState.isGeminiConnected) HologramGreen else Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // 2. Hardware & Permissions Status Card
                item {
                    GlassPanel(title = "PERMISSIONS & ACCESS", accent = accent) {
                        PermissionRow(
                            label = "Microphone",
                            icon = Icons.Default.Mic,
                            isGranted = uiState.hasMicPermission,
                            accent = accent
                        )
                        PermissionRow(
                            label = "Contacts",
                            icon = Icons.Default.Contacts,
                            isGranted = uiState.hasContactsPermission,
                            accent = accent
                        )
                        PermissionRow(
                            label = "Phone Calls",
                            icon = Icons.Default.Call,
                            isGranted = uiState.hasPhonePermission,
                            accent = accent
                        )
                        PermissionRow(
                            label = "Notifications",
                            icon = Icons.Default.Notifications,
                            isGranted = uiState.hasNotificationPermission,
                            accent = accent
                        )

                        if (!uiState.hasMicPermission || !uiState.hasContactsPermission || !uiState.hasPhonePermission) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accent.copy(alpha = 0.12f))
                                    .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { onRequestPermissions() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Grant Missing Permissions",
                                    color = accent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // 3. Voice & Assistant Behavior
                item {
                    GlassPanel(title = "ASSISTANT CORE BEHAVIOR", accent = accent) {
                        ToggleRow(
                            title = "Wake-Word Detection",
                            subtitle = "Listen continuously for 'X' or 'Hey X'",
                            icon = Icons.Default.Hearing,
                            checked = uiState.wakeWordEnabled,
                            accent = accent,
                            onCheckedChange = { viewModel.toggleWakeWord(it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ToggleRow(
                            title = "Background Assistant Service",
                            subtitle = "Keep X ready even when app is closed",
                            icon = Icons.Default.Security,
                            checked = uiState.backgroundServiceEnabled,
                            accent = accent,
                            onCheckedChange = { viewModel.toggleBackgroundService(it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ToggleRow(
                            title = "Anime Companion Animations",
                            subtitle = "Breathing, blinking, and speaking visuals",
                            icon = Icons.Default.Animation,
                            checked = uiState.companionAnimationEnabled,
                            accent = accent,
                            onCheckedChange = { viewModel.toggleCompanionAnimation(it) }
                        )
                    }
                }

                // 4. Voice Persona & Synthesis
                item {
                    GlassPanel(title = "VOICE SYNTHESIS", accent = accent) {
                        var expandedVoice by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedVoice = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Voice Persona",
                                        color = TextWhite,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = uiState.voiceTone.label,
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Text(
                                text = "Change ▸",
                                color = accent,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        DropdownMenu(
                            expanded = expandedVoice,
                            onDismissRequest = { expandedVoice = false },
                            modifier = Modifier.background(CyberDarkCharcoal)
                        ) {
                            VoiceTone.values().forEach { tone ->
                                DropdownMenuItem(
                                    text = { Text(tone.label, color = TextWhite) },
                                    onClick = {
                                        viewModel.setVoiceTone(tone)
                                        expandedVoice = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. Visual Customization & Cyber Theme
                item {
                    GlassPanel(title = "VISUAL INTERFACE", accent = accent) {
                        Text(
                            text = "Neon Theme Accent",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThemeAccent.values().forEach { itemAccent ->
                                val isSelected = uiState.themeAccent == itemAccent
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) itemAccent.primaryColor.copy(alpha = 0.2f) else CyberSurface)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) itemAccent.primaryColor else CyberGlass,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setThemeAccent(itemAccent) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(itemAccent.primaryColor)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = itemAccent.name.lowercase().replaceFirstChar { it.uppercase() },
                                            color = if (isSelected) TextWhite else TextMuted,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Animation Intensity",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnimationIntensity.values().forEach { intensity ->
                                val isSelected = uiState.animationIntensity == intensity
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) accent.copy(alpha = 0.2f) else CyberSurface)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) accent else CyberGlass,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setAnimationIntensity(intensity) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = intensity.name,
                                        color = if (isSelected) TextWhite else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. About X Section with Creator Wasil
                item {
                    GlassPanel(title = "ABOUT X", accent = accent) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "X Cyber AI Companion",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Version 2.0 • Holographic HUD Architecture",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Creator Wasil displayed subtly in About section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Creator",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Wasil",
                                color = accent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Zero-touch voice assistant powered by Gemini Live with real-time audio analysis, original anime companion avatar, and autonomous device tools.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Subtle footer in Settings
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Engineered by Wasil • X Neural System",
                            color = TextMuted.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassPanel(
    title: String,
    accent: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberGlass),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun PermissionRow(
    label: String,
    icon: ImageVector,
    isGranted: Boolean,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                color = TextWhite,
                fontSize = 13.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isGranted) HologramGreen else Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isGranted) "Granted" else "Missing",
                color = if (isGranted) HologramGreen else Color(0xFFEF4444),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent,
                uncheckedTrackColor = CyberDarkCharcoal,
                uncheckedThumbColor = TextMuted
            )
        )
    }
}
