package com.msterjime.zikirdua

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

private const val AzanPlaybackChannelId = "azan_playback"
private const val AzanPlaybackNotificationId = 91001
private const val AzanPlaybackActionStop = "com.msterjime.zikirdua.STOP_AZAN"

internal fun startAzanPlayback(context: Context) {
    val intent = Intent(context, AzanPlaybackService::class.java)
    ContextCompat.startForegroundService(context, intent)
}

internal fun stopAzanPlayback(context: Context) {
    val intent = Intent(context, AzanPlaybackService::class.java).apply {
        action = AzanPlaybackActionStop
    }
    ContextCompat.startForegroundService(context, intent)
}

class AzanPlaybackService : Service() {
    private var player: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createAzanChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == AzanPlaybackActionStop) {
            stopCurrentPlayback()
            stopSelf()
            return START_NOT_STICKY
        }

        val preferences = getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
        var selected = preferences.getString("azan_sound", "Azan 1").orEmpty()

        // Migrate temporary/generated melody choices from previous builds.
        selected = when (selected) {
            "Melody 1", "Arabic Melody", "Turkish Melody" -> "Melody Oasis"
            else -> selected
        }
        preferences.edit().putString("azan_sound", selected).apply()

        val notification = NotificationCompat.Builder(this, AzanPlaybackChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Namaz wagty")
            .setContentText(selected)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        startForeground(AzanPlaybackNotificationId, notification)
        stopCurrentPlayback()

        val melodyEnabled = preferences.getBoolean("melody_enabled", true)
        val soundRes = when (selected) {
            "Melody Oasis" -> if (melodyEnabled) R.raw.melody_oasis else null
            "Melody Nasheed" -> if (melodyEnabled) R.raw.melody_nasheed else null
            "Melody Ney" -> if (melodyEnabled) R.raw.melody_ney else null
            "Azan 2" -> R.raw.azan_2
            else -> R.raw.azan_1
        }

        if (soundRes == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        player = MediaPlayer.create(this, soundRes)?.apply {
            setVolume(1f, 1f)
            setOnCompletionListener {
                it.release()
                player = null
                stopSelf()
            }
            setOnErrorListener { mp, _, _ ->
                mp.release()
                player = null
                stopSelf()
                true
            }
            start()
        }

        if (player == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Foreground playback continues after the activity is closed or removed
        // from Recent Apps. Android may restart the service if the process is killed.
        return START_STICKY
    }

    private fun stopCurrentPlayback() {
        player?.runCatching {
            if (isPlaying) stop()
            release()
        }
        player = null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Do not stop playback when the user closes the app from Recent Apps.
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        stopCurrentPlayback()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createAzanChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AzanPlaybackChannelId,
                "Azan playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Azan and prayer notification melody playback"
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
