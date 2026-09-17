package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SynthMagenta

enum class AssistantState(val displayName: String) {
    IDLE("STANDBY"),
    LISTENING("LISTENING"),
    THINKING("THINKING"),
    SPEAKING("SPEAKING")
}

enum class ThemeAccent(val label: String, val primaryColor: Color) {
    CYAN("Electric Cyan", ElectricCyan),
    VIOLET("Neon Violet", NeonViolet),
    BLUE("Cyber Blue", CyberBlue),
    MAGENTA("Synth Magenta", SynthMagenta)
}

enum class AnimationIntensity(val label: String) {
    HIGH("High (60 FPS + Particles)"),
    MEDIUM("Balanced"),
    LOW("Battery Saver")
}

enum class VoiceTone(val label: String, val geminiVoice: String) {
    NOVA("Nova (Warm & Clear)", "Nova"),
    ZEPHYR("Zephyr (Calm & Futuristic)", "Zephyr"),
    KORE("Kore (Melodic Anime AI)", "Kore"),
    PUCK("Puck (Playful)", "Puck"),
    AOEDE("Aoede (Elegance)", "Aoede")
}

data class AssistantAction(
    val title: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)
