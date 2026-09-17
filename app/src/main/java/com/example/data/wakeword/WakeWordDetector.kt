package com.example.data.wakeword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class WakeWordDetector(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val tag = "WakeWordDetector"
    private var recognizer: SpeechRecognizer? = null
    private var isListeningForWakeWord = false
    private var restartJob: Job? = null

    var onWakeWordDetected: (() -> Unit)? = null

    fun startListening() {
        if (isListeningForWakeWord) return
        isListeningForWakeWord = true
        coroutineScope.launch(Dispatchers.Main) {
            initAndStartRecognizer()
        }
    }

    fun stopListening() {
        isListeningForWakeWord = false
        restartJob?.cancel()
        coroutineScope.launch(Dispatchers.Main) {
            try {
                recognizer?.stopListening()
                recognizer?.destroy()
                recognizer = null
            } catch (e: Exception) {
                Log.e(tag, "Error stopping wake word recognizer", e)
            }
        }
    }

    private fun initAndStartRecognizer() {
        if (!isListeningForWakeWord) return
        try {
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    scheduleRestart()
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.joinToString(" ")?.lowercase() ?: ""
                    Log.d(tag, "Wake-word probe heard: $text")

                    if (checkTrigger(text)) {
                        onWakeWordDetected?.invoke()
                    } else {
                        scheduleRestart()
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.joinToString(" ")?.lowercase() ?: ""
                    if (checkTrigger(text)) {
                        onWakeWordDetected?.invoke()
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
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(tag, "Wake word recognizer exception", e)
            scheduleRestart()
        }
    }

    private fun checkTrigger(text: String): Boolean {
        return text.contains("hey x") ||
                text.contains("hello x") ||
                text.contains("assistant x") ||
                text.trim() == "x" ||
                text.startsWith("x ") ||
                text.endsWith(" x")
    }

    private fun scheduleRestart() {
        if (!isListeningForWakeWord) return
        restartJob?.cancel()
        restartJob = coroutineScope.launch(Dispatchers.Main) {
            delay(1200)
            if (isActive && isListeningForWakeWord) {
                initAndStartRecognizer()
            }
        }
    }
}
