package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberGlass
import com.example.ui.theme.CyberNearBlack
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HologramGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AssistantUiState
import com.example.ui.viewmodel.XAssistantViewModel

@Composable
fun OnboardingScreen(
    uiState: AssistantUiState,
    viewModel: XAssistantViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = uiState.themeAccent.primaryColor

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.checkPermissions() }

    val contactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.checkPermissions() }

    val phoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.checkPermissions() }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.checkPermissions() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen_container")
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberBlack, CyberNearBlack, CyberBlack)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))

                // Futuristic Sub-badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberGlass)
                        .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SYSTEM INITIALIZATION",
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Title required by user: "Let's get X ready."
                Text(
                    text = "Let's get X ready.",
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enable neural interfaces for voice control, calling, and companion background alerts.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(26.dp))

                // 4 Permission Cards
                // Card 1: 🎙 Microphone
                PermissionSetupCard(
                    title = "Microphone",
                    explanation = "Required for real-time speech interaction, zero-touch wake-word detection, and audio waveform synthesis.",
                    icon = Icons.Default.Mic,
                    isGranted = uiState.hasMicPermission,
                    accent = accent,
                    onEnable = { micLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Card 2: 👤 Contacts
                PermissionSetupCard(
                    title = "Contacts",
                    explanation = "Allows X to look up contact numbers when you request to call or message someone by name.",
                    icon = Icons.Default.Contacts,
                    isGranted = uiState.hasContactsPermission,
                    accent = accent,
                    onEnable = { contactsLauncher.launch(android.Manifest.permission.READ_CONTACTS) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Card 3: 📞 Phone
                PermissionSetupCard(
                    title = "Phone",
                    explanation = "Enables hands-free autonomous phone calling through device telephony when commanded.",
                    icon = Icons.Default.Call,
                    isGranted = uiState.hasPhonePermission,
                    accent = accent,
                    onEnable = { phoneLauncher.launch(android.Manifest.permission.CALL_PHONE) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Card 4: 🔔 Notifications
                PermissionSetupCard(
                    title = "Notifications",
                    explanation = "Maintains the foreground companion status bar notification for background wake-word listening.",
                    icon = Icons.Default.Notifications,
                    isGranted = uiState.hasNotificationPermission,
                    accent = accent,
                    onEnable = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.checkPermissions()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Completion / Enter X Core Button
            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = CyberBlack
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_complete_button")
            ) {
                Text(
                    text = if (uiState.hasMicPermission) "ENTER X CORE" else "CONTINUE TO X",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun PermissionSetupCard(
    title: String,
    explanation: String,
    icon: ImageVector,
    isGranted: Boolean,
    accent: Color,
    onEnable: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberGlass),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isGranted) HologramGreen.copy(alpha = 0.5f) else accent.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .testTag("permission_card_${title.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) HologramGreen.copy(alpha = 0.15f) else accent.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isGranted) HologramGreen else accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isGranted) {
                        AnimatedVisibility(visible = true, enter = fadeIn()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = HologramGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Ready",
                                    color = HologramGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = explanation,
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (!isGranted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.15f))
                        .border(1.dp, accent, RoundedCornerShape(10.dp))
                        .clickable { onEnable() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Enable",
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(HologramGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Granted",
                        tint = HologramGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
