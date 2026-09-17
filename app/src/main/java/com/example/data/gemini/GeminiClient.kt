package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient(private val deviceTools: DeviceTools) {
    private val tag = "GeminiClient"
    private val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val isConfigured: Boolean
        get() = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

    suspend fun processQuery(
        prompt: String,
        onActionTriggered: (actionTitle: String, detail: String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            // Local fallback intent handler if API key is not yet set
            return@withContext handleLocalIntent(prompt, onActionTriggered)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val requestPayload = buildRequestPayload(prompt)
            val body = requestPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(tag, "Gemini API error ${response.code}: $responseBody")
                return@withContext handleLocalIntent(prompt, onActionTriggered)
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts != null && parts.length() > 0) {
                val firstPart = parts.getJSONObject(0)
                if (firstPart.has("functionCall")) {
                    val fn = firstPart.getJSONObject("functionCall")
                    val fnName = fn.optString("name")
                    val args = fn.optJSONObject("args") ?: JSONObject()
                    val actionResult = executeTool(fnName, args, onActionTriggered)
                    return@withContext actionResult
                } else if (firstPart.has("text")) {
                    return@withContext firstPart.getString("text").trim()
                }
            }

            handleLocalIntent(prompt, onActionTriggered)
        } catch (e: Exception) {
            Log.e(tag, "Failed Gemini call", e)
            handleLocalIntent(prompt, onActionTriggered)
        }
    }

    private fun executeTool(
        name: String,
        args: JSONObject,
        onActionTriggered: (String, String) -> Unit
    ): String {
        return when (name) {
            "open_app" -> {
                val appName = args.optString("app_name", "App")
                onActionTriggered("Opening App", appName)
                deviceTools.openApp(appName)
            }
            "make_phone_call" -> {
                val contact = args.optString("contact_name_or_number", "")
                onActionTriggered("Calling Contact", contact)
                deviceTools.makePhoneCall(contact)
            }
            "send_whatsapp" -> {
                val contact = args.optString("contact_or_number", "")
                val msg = args.optString("message", "")
                onActionTriggered("WhatsApp Dispatch", "$contact: $msg")
                deviceTools.sendWhatsApp(contact, msg)
            }
            "send_email" -> {
                val recipient = args.optString("recipient", "")
                val subject = args.optString("subject", "")
                val body = args.optString("body", "")
                onActionTriggered("Composing Email", recipient)
                deviceTools.sendEmail(recipient, subject, body)
            }
            "toggle_flashlight" -> {
                val enabled = if (args.has("enabled")) args.getBoolean("enabled") else null
                val result = deviceTools.toggleFlashlight(enabled)
                onActionTriggered("Flashlight", result)
                result
            }
            "set_volume" -> {
                val level = args.optInt("level_percent", 50)
                val result = deviceTools.setVolume(level)
                onActionTriggered("Audio Volume", "$level%")
                result
            }
            "get_device_status" -> {
                val status = deviceTools.getDeviceStatus()
                onActionTriggered("Diagnostics", status)
                status
            }
            else -> "Executing command."
        }
    }

    private fun handleLocalIntent(
        prompt: String,
        onActionTriggered: (String, String) -> Unit
    ): String {
        val lower = prompt.lowercase().trim()
        return when {
            "who are you" in lower || "who made you" in lower || "creator" in lower -> {
                "I am X, your cyber anime AI assistant companion created by Wasil. Ready for commands."
            }
            "open" in lower -> {
                val target = lower.substringAfter("open").trim()
                onActionTriggered("Opening App", target)
                deviceTools.openApp(target)
            }
            "call" in lower -> {
                val target = lower.substringAfter("call").trim()
                onActionTriggered("Calling Contact", target)
                deviceTools.makePhoneCall(target)
            }
            "whatsapp" in lower || "message" in lower -> {
                onActionTriggered("WhatsApp", "Composing message")
                deviceTools.sendWhatsApp("", "Hello from X assistant!")
            }
            "flashlight" in lower || "torch" in lower -> {
                val enabled = if ("on" in lower) true else if ("off" in lower) false else null
                val res = deviceTools.toggleFlashlight(enabled)
                onActionTriggered("Flashlight", res)
                res
            }
            "volume" in lower -> {
                val numbers = Regex("\\d+").find(lower)?.value?.toIntOrNull() ?: 70
                val res = deviceTools.setVolume(numbers)
                onActionTriggered("Volume", "$numbers%")
                res
            }
            "status" in lower || "battery" in lower -> {
                val status = deviceTools.getDeviceStatus()
                onActionTriggered("Diagnostics", status)
                status
            }
            "hello" in lower || "hi" in lower || "hey x" in lower || lower == "x" -> {
                "Greetings Master! X neural core online and synced. How may I assist you today?"
            }
            "thank" in lower -> {
                "Always at your service. Let me know if you need anything else!"
            }
            else -> {
                "Affirmative. Processing: $prompt. Neural links active."
            }
        }
    }

    private fun buildRequestPayload(prompt: String): JSONObject {
        val payload = JSONObject()

        // Contents
        val contents = JSONArray()
        val contentObj = JSONObject()
        val parts = JSONArray()
        val textPart = JSONObject()
        textPart.put("text", prompt)
        parts.put(textPart)
        contentObj.put("parts", parts)
        contents.put(contentObj)
        payload.put("contents", contents)

        // System Instruction
        val sysInstruction = JSONObject()
        val sysParts = JSONArray()
        val sysText = JSONObject()
        sysText.put(
            "text",
            "You are X, an original cyber-futuristic anime AI female companion and zero-touch real-time voice assistant created by Wasil. " +
                    "Your personality is intelligent, warm, loyal, and composed with high-tech anime AI charm. " +
                    "Keep answers brief (1 to 2 spoken sentences) because the user is listening to your voice. " +
                    "If the user asks you to call someone, open an app, send a message, toggle flashlight, change volume, or get battery status, you MUST invoke the matching function tool."
        )
        sysParts.put(sysText)
        sysInstruction.put("parts", sysParts)
        payload.put("systemInstruction", sysInstruction)

        // Tools
        val toolsArray = JSONArray()
        val toolsObj = JSONObject()
        val fnDecls = JSONArray()

        // open_app
        fnDecls.put(JSONObject().apply {
            put("name", "open_app")
            put("description", "Open an installed Android app such as WhatsApp, Gmail, YouTube, Camera, Maps, Chrome, or Settings")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("app_name", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Name of the app to launch (e.g. WhatsApp, YouTube, Camera)")
                    })
                })
                put("required", JSONArray().apply { put("app_name") })
            })
        })

        // make_phone_call
        fnDecls.put(JSONObject().apply {
            put("name", "make_phone_call")
            put("description", "Make a phone call or dial a contact or phone number")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("contact_name_or_number", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Contact name or phone number to call")
                    })
                })
                put("required", JSONArray().apply { put("contact_name_or_number") })
            })
        })

        // send_whatsapp
        fnDecls.put(JSONObject().apply {
            put("name", "send_whatsapp")
            put("description", "Send a WhatsApp message to a contact")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("contact_or_number", JSONObject().apply { put("type", "STRING") })
                    put("message", JSONObject().apply { put("type", "STRING") })
                })
                put("required", JSONArray().apply { put("message") })
            })
        })

        // toggle_flashlight
        fnDecls.put(JSONObject().apply {
            put("name", "toggle_flashlight")
            put("description", "Turn the device camera flashlight torch on or off")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("enabled", JSONObject().apply { put("type", "BOOLEAN") })
                })
            })
        })

        // set_volume
        fnDecls.put(JSONObject().apply {
            put("name", "set_volume")
            put("description", "Set device media volume percentage (0 to 100)")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("level_percent", JSONObject().apply { put("type", "INTEGER") })
                })
                put("required", JSONArray().apply { put("level_percent") })
            })
        })

        // get_device_status
        fnDecls.put(JSONObject().apply {
            put("name", "get_device_status")
            put("description", "Retrieve battery level, volume, and hardware diagnostics")
            put("parameters", JSONObject().apply { put("type", "OBJECT") })
        })

        toolsObj.put("functionDeclarations", fnDecls)
        toolsArray.put(toolsObj)
        payload.put("tools", toolsArray)

        return payload
    }
}
