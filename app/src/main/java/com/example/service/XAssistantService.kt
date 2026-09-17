package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.wakeword.WakeWordDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class XAssistantService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var wakeWordDetector: WakeWordDetector? = null

    companion object {
        const val CHANNEL_ID = "x_assistant_foreground_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_START = "ACTION_START_X_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_X_SERVICE"

        fun start(context: Context) {
            val intent = Intent(context, XAssistantService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, XAssistantService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())

        if (wakeWordDetector == null) {
            wakeWordDetector = WakeWordDetector(this, serviceScope).apply {
                onWakeWordDetected = {
                    val launchIntent = Intent(this@XAssistantService, MainActivity::class.java).apply {
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("TRIGGER_VOICE", true)
                    }
                    startActivity(launchIntent)
                }
                startListening()
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "X Companion Background Core",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground background service maintaining X assistant readiness"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("X Neural AI Online")
            .setContentText("Listening for wake-word • Zero-touch active")
            .setSmallIcon(R.drawable.img_app_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeWordDetector?.stopListening()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
