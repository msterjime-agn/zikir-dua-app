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

internal fun startAzanPlayback(context: Context) {
    val intent = Intent(context, AzanPlaybackService::class.java)
    ContextCompat.startForegroundService(context, intent)
}

class AzanPlaybackService : Service() {
    private var player: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createAzanChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, AzanPlaybackChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Namaz wagty")
            .setContentText("Azan")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        startForeground(AzanPlaybackNotificationId, notification)

        if (player?.isPlaying == true) {
            return START_NOT_STICKY
        }

        val preferences = getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
        val selected = preferences.getString("azan_sound", "Azan 1").orEmpty()
        val soundRes = if (selected.contains("2")) R.raw.azan_2 else R.raw.azan_1

        player?.release()
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
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player?.runCatching {
            if (isPlaying) stop()
            release()
        }
        player = null
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
                description = "Azan playback service"
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
