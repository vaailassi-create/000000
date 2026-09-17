package com.example.data.gemini

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.ContactsContract
import android.util.Log

class DeviceTools(private val context: Context) {
    private val tag = "DeviceTools"
    private var isTorchOn = false

    fun openApp(appName: String): String {
        return try {
            val pm = context.packageManager
            val lower = appName.lowercase()
            val packageName = when {
                "whatsapp" in lower -> "com.whatsapp"
                "gmail" in lower || "mail" in lower -> "com.google.android.gm"
                "youtube" in lower -> "com.google.android.youtube"
                "chrome" in lower || "browser" in lower -> "com.android.chrome"
                "camera" in lower -> {
                    val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return "Opened Camera"
                }
                "map" in lower -> "com.google.android.apps.maps"
                "setting" in lower -> {
                    val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return "Opened Settings"
                }
                else -> null
            }

            if (packageName != null) {
                val launchIntent = pm.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return "Opened $appName"
                }
            }

            // Generic search across installed packages
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(intent, 0)
            for (app in apps) {
                val label = app.loadLabel(pm).toString().lowercase()
                if (label.contains(lower) || lower.contains(label)) {
                    val launch = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
                    if (launch != null) {
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launch)
                        return "Opened ${app.loadLabel(pm)}"
                    }
                }
            }
            "App '$appName' not found on device"
        } catch (e: Exception) {
            Log.e(tag, "Failed to open app $appName", e)
            "Could not open $appName: ${e.message}"
        }
    }

    fun makePhoneCall(query: String): String {
        return try {
            var phoneNumber = query.filter { it.isDigit() || it == '+' }
            var contactName = query

            if (phoneNumber.isEmpty() || phoneNumber.length < 3) {
                // Look up in contacts
                val foundNumber = findPhoneNumber(query)
                if (foundNumber != null) {
                    phoneNumber = foundNumber
                } else {
                    phoneNumber = query
                }
            }

            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Check if call permission is available, fallback to dialer
            try {
                context.startActivity(callIntent)
                "Calling $contactName ($phoneNumber)"
            } catch (se: SecurityException) {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                "Dialing $contactName ($phoneNumber)"
            }
        } catch (e: Exception) {
            Log.e(tag, "Error making call to $query", e)
            "Could not call $query: ${e.message}"
        }
    }

    fun sendWhatsApp(contactOrNumber: String, message: String): String {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Composed WhatsApp message to $contactOrNumber"
        } catch (e: Exception) {
            // Fallback generic send
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Send message").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                "Shared message: $message"
            } catch (ex: Exception) {
                "WhatsApp not installed or error: ${e.message}"
            }
        }
    }

    fun sendEmail(recipient: String, subject: String, body: String): String {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$recipient")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Composing email to $recipient"
        } catch (e: Exception) {
            Log.e(tag, "Error sending email", e)
            "Could not open email: ${e.message}"
        }
    }

    fun toggleFlashlight(enabled: Boolean? = null): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return "No flash found"
            val target = enabled ?: !isTorchOn
            cameraManager.setTorchMode(cameraId, target)
            isTorchOn = target
            if (isTorchOn) "Flashlight turned on" else "Flashlight turned off"
        } catch (e: CameraAccessException) {
            "Flashlight access error: ${e.message}"
        } catch (e: Exception) {
            "Flashlight error: ${e.message}"
        }
    }

    fun setVolume(levelPercent: Int): String {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = ((levelPercent / 100f) * maxVol).toInt().coerceIn(0, maxVol)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
            "Volume set to $levelPercent%"
        } catch (e: Exception) {
            "Could not set volume: ${e.message}"
        }
    }

    fun getDeviceStatus(): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val volPct = if (maxVol > 0) (curVol * 100) / maxVol else 0

        return "Battery: $batteryLevel%, Volume: $volPct%, Flashlight: ${if (isTorchOn) "ON" else "OFF"}"
    }

    private fun findPhoneNumber(contactName: String): String? {
        val cr = context.contentResolver
        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$contactName%"),
            null
        ) ?: return null

        cursor.use {
            if (it.moveToFirst()) {
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numberIndex != -1) {
                    return it.getString(numberIndex)
                }
            }
        }
        return null
    }
}
