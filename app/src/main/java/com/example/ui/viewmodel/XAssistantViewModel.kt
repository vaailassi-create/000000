package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.AudioStreamingManager
import com.example.data.gemini.DeviceTools
import com.example.data.gemini.GeminiClient
import com.example.data.model.AnimationIntensity
import com.example.data.model.AssistantAction
import com.example.data.model.AssistantState
import com.example.data.model.ThemeAccent
import com.example.data.model.VoiceTone
import com.example.data.wakeword.WakeWordDetector
import com.example.service.XAssistantService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    SETTINGS,
    ONBOARDING
}

data class AssistantUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val state: AssistantState = AssistantState.IDLE,
    val recognizedText: String = "",
    val spokenResponse: String = "",
    val activeAction: AssistantAction? = null,
    val micAmplitude: Float = 0f,
    val outputAmplitude: Float = 0f,
    val themeAccent: ThemeAccent = ThemeAccent.CYAN,
    val voiceTone: VoiceTone = VoiceTone.KORE,
    val voicePitch: Float = 1.2f,
    val voiceRate: Float = 1.05f,
    val animationIntensity: AnimationIntensity = AnimationIntensity.HIGH,
    val companionAnimationEnabled: Boolean = true,
    val wakeWordEnabled: Boolean = true,
    val backgroundServiceEnabled: Boolean = false,
    val isGeminiConnected: Boolean = false,
    // Permissions
    val hasMicPermission: Boolean = false,
    val hasContactsPermission: Boolean = false,
    val hasPhonePermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isFirstLaunch: Boolean = false
)

class XAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication()

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val deviceTools = DeviceTools(context)
    val geminiClient = GeminiClient(deviceTools)
    private val audioManager = AudioStreamingManager(context, viewModelScope)
    private val wakeWordDetector = WakeWordDetector(context, viewModelScope)

    init {
        checkPermissions()
        _uiState.update {
            it.copy(
                isGeminiConnected = geminiClient.isConfigured,
                isFirstLaunch = !it.hasMicPermission
            )
        }

        // Setup Audio streaming listeners
        audioManager.onSpeechRecognized = { text ->
            handleSpeechInput(text)
        }
        audioManager.onSpeechError = { errorMsg ->
            if (_uiState.value.state == AssistantState.LISTENING) {
                _uiState.update { it.copy(state = AssistantState.IDLE, recognizedText = "") }
            }
        }
        audioManager.onTtsStart = {
            _uiState.update { it.copy(state = AssistantState.SPEAKING) }
        }
        audioManager.onTtsDone = {
            _uiState.update { it.copy(state = AssistantState.IDLE, activeAction = null) }
            if (_uiState.value.wakeWordEnabled) {
                wakeWordDetector.startListening()
            }
        }

        // Setup Wake-word listener
        wakeWordDetector.onWakeWordDetected = {
            onWakeWordTriggered()
        }

        if (_uiState.value.wakeWordEnabled && _uiState.value.hasMicPermission) {
            wakeWordDetector.startListening()
        }

        // Collect mic and output amplitudes
        viewModelScope.launch {
            audioManager.micAmplitude.collect { amp ->
                _uiState.update { it.copy(micAmplitude = amp) }
            }
        }
        viewModelScope.launch {
            audioManager.outputAmplitude.collect { amp ->
                _uiState.update { it.copy(outputAmplitude = amp) }
            }
        }
    }

    fun checkPermissions() {
        val mic = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val contacts = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val phone = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        _uiState.update {
            it.copy(
                hasMicPermission = mic,
                hasContactsPermission = contacts,
                hasPhonePermission = phone,
                hasNotificationPermission = notif
            )
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun startListening() {
        if (!_uiState.value.hasMicPermission) {
            navigateTo(AppScreen.ONBOARDING)
            return
        }
        wakeWordDetector.stopListening()
        audioManager.stopSpeaking()
        _uiState.update {
            it.copy(
                state = AssistantState.LISTENING,
                recognizedText = "",
                spokenResponse = "",
                activeAction = null
            )
        }
        audioManager.startListening()
    }

    fun stopListening() {
        if (_uiState.value.state == AssistantState.LISTENING) {
            audioManager.stopListening()
            _uiState.update { it.copy(state = AssistantState.THINKING) }
        }
    }

    private fun handleSpeechInput(text: String) {
        audioManager.stopListening()
        _uiState.update {
            it.copy(
                state = AssistantState.THINKING,
                recognizedText = text
            )
        }

        viewModelScope.launch {
            val response = geminiClient.processQuery(text) { actionTitle, detail ->
                _uiState.update {
                    it.copy(activeAction = AssistantAction(title = actionTitle, detail = detail))
                }
            }

            _uiState.update {
                it.copy(spokenResponse = response)
            }
            audioManager.speak(response)
        }
    }

    fun cancelInteraction() {
        audioManager.stopListening()
        audioManager.stopSpeaking()
        _uiState.update {
            it.copy(
                state = AssistantState.IDLE,
                recognizedText = "",
                activeAction = null
            )
        }
        if (_uiState.value.wakeWordEnabled && _uiState.value.hasMicPermission) {
            wakeWordDetector.startListening()
        }
    }

    private fun onWakeWordTriggered() {
        startListening()
    }

    fun toggleWakeWord(enabled: Boolean) {
        _uiState.update { it.copy(wakeWordEnabled = enabled) }
        if (enabled && _uiState.value.hasMicPermission) {
            wakeWordDetector.startListening()
        } else {
            wakeWordDetector.stopListening()
        }
    }

    fun toggleBackgroundService(enabled: Boolean) {
        _uiState.update { it.copy(backgroundServiceEnabled = enabled) }
        if (enabled) {
            XAssistantService.start(context)
        } else {
            XAssistantService.stop(context)
        }
    }

    fun setThemeAccent(accent: ThemeAccent) {
        _uiState.update { it.copy(themeAccent = accent) }
    }

    fun setVoiceTone(tone: VoiceTone) {
        _uiState.update { it.copy(voiceTone = tone) }
        when (tone) {
            VoiceTone.KORE -> audioManager.updateVoicePitchAndRate(1.22f, 1.05f)
            VoiceTone.NOVA -> audioManager.updateVoicePitchAndRate(1.0f, 1.0f)
            VoiceTone.ZEPHYR -> audioManager.updateVoicePitchAndRate(0.95f, 0.98f)
            VoiceTone.PUCK -> audioManager.updateVoicePitchAndRate(1.3f, 1.15f)
            VoiceTone.AOEDE -> audioManager.updateVoicePitchAndRate(1.1f, 0.95f)
        }
    }

    fun setAnimationIntensity(intensity: AnimationIntensity) {
        _uiState.update { it.copy(animationIntensity = intensity) }
    }

    fun toggleCompanionAnimation(enabled: Boolean) {
        _uiState.update { it.copy(companionAnimationEnabled = enabled) }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
        wakeWordDetector.stopListening()
    }
}
