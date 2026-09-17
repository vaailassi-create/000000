package com.example.data.audio

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

class AudioStreamingManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private val tag = "AudioStreamManager"

    private val _micAmplitude = MutableStateFlow(0f)
    val micAmplitude: StateFlow<Float> = _micAmplitude.asStateFlow()

    private val _outputAmplitude = MutableStateFlow(0f)
    val outputAmplitude: StateFlow<Float> = _outputAmplitude.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private var audioRecordJob: Job? = null
    private var outputAmpJob: Job? = null
    private var isRecording = false

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null
    var onTtsStart: (() -> Unit)? = null
    var onTtsDone: (() -> Unit)? = null

    init {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e(tag, "TTS init failure", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val result = tts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.getDefault())
                }
                tts.setPitch(1.22f) // Anime AI companion slightly melodic bright pitch
                tts.setSpeechRate(1.05f) // Energetic and modern
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onTtsStart?.invoke()
                        startOutputAmplitudeSimulation()
                    }

                    override fun onDone(utteranceId: String?) {
                        stopOutputAmplitudeSimulation()
                        onTtsDone?.invoke()
                    }

                    override fun onError(utteranceId: String?) {
                        stopOutputAmplitudeSimulation()
                        onTtsDone?.invoke()
                    }
                })
                isTtsReady = true
            }
        }
    }

    fun startListening() {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize rmsdB (typically -2 to 10) to 0.0 .. 1.0
                        val norm = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _micAmplitude.value = norm
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _micAmplitude.value = 0f
                    }
                    override fun onError(error: Int) {
                        _micAmplitude.value = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Assistant busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                            else -> "Audio error"
                        }
                        onSpeechError?.invoke(msg)
                    }
                    override fun onResults(results: Bundle?) {
                        _micAmplitude.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            onSpeechRecognized?.invoke(text)
                        } else {
                            onSpeechError?.invoke("No input heard")
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()
                        if (!partial.isNullOrBlank()) {
                            _micAmplitude.value = 0.5f
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                speechRecognizer?.startListening(intent)
                startPcmAmplitudeTracking()
            } catch (e: Exception) {
                Log.e(tag, "SpeechRecognizer start failure", e)
                onSpeechError?.invoke(e.message ?: "Recognition failed")
            }
        }
    }

    fun stopListening() {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(tag, "Error stopping listener", e)
            }
            stopPcmAmplitudeTracking()
            _micAmplitude.value = 0f
        }
    }

    private fun startPcmAmplitudeTracking() {
        if (isRecording) return
        isRecording = true
        audioRecordJob = coroutineScope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            if (minBufSize <= 0) return@launch

            var record: AudioRecord? = null
            try {
                record = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufSize * 2
                )
                if (record.state != AudioRecord.STATE_INITIALIZED) {
                    record.release()
                    return@launch
                }
                record.startRecording()
                val buffer = ShortArray(minBufSize)

                while (isActive && isRecording) {
                    val read = record.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var maxAmp = 0
                        for (i in 0 until read) {
                            val absVal = abs(buffer[i].toInt())
                            if (absVal > maxAmp) maxAmp = absVal
                        }
                        val normalized = (maxAmp / 15000f).coerceIn(0f, 1f)
                        _micAmplitude.value = normalized
                    }
                    kotlinx.coroutines.delay(40)
                }
            } catch (e: SecurityException) {
                Log.w(tag, "No mic permission for PCM streaming")
            } catch (e: Exception) {
                Log.w(tag, "PCM recording exception: ${e.message}")
            } finally {
                try {
                    record?.stop()
                    record?.release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    private fun stopPcmAmplitudeTracking() {
        isRecording = false
        audioRecordJob?.cancel()
        audioRecordJob = null
    }

    fun speak(text: String) {
        if (!isTtsReady || textToSpeech == null) {
            onTtsDone?.invoke()
            return
        }
        val utteranceId = "x_utterance_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping TTS", e)
        }
        stopOutputAmplitudeSimulation()
    }

    fun updateVoicePitchAndRate(pitch: Float, rate: Float) {
        textToSpeech?.setPitch(pitch)
        textToSpeech?.setSpeechRate(rate)
    }

    private fun startOutputAmplitudeSimulation() {
        outputAmpJob?.cancel()
        outputAmpJob = coroutineScope.launch(Dispatchers.Default) {
            var step = 0f
            while (isActive) {
                step += 0.25f
                // Generate dynamic speech cadence waveform
                val amp = ((sin(step.toDouble()) * 0.45 + sin((step * 2.3).toDouble()) * 0.35 + 0.5)).toFloat().coerceIn(0.1f, 1f)
                _outputAmplitude.value = amp
                kotlinx.coroutines.delay(50)
            }
        }
    }

    private fun stopOutputAmplitudeSimulation() {
        outputAmpJob?.cancel()
        outputAmpJob = null
        _outputAmplitude.value = 0f
    }

    fun release() {
        stopPcmAmplitudeTracking()
        stopOutputAmplitudeSimulation()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(tag, "Error releasing audio manager", e)
        }
    }
}
