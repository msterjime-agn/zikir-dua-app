package com.msterjime.zikirdua

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
    private var toneGenerator: ToneGenerator? = null
    private val handler = Handler(Looper.getMainLooper())

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
        val selected = preferences.getString("azan_sound", "Azan 1").orEmpty()

        val notification = NotificationCompat.Builder(this, AzanPlaybackChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Namaz wagty")
            .setContentText(selected)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        startForeground(AzanPlaybackNotificationId, notification)
        stopCurrentPlayback()

        when (selected) {
            "Melody 1" -> playShortMelody(
                listOf(
                    ToneGenerator.TONE_DTMF_4 to 180,
                    ToneGenerator.TONE_DTMF_6 to 180,
                    ToneGenerator.TONE_DTMF_8 to 260
                )
            )
            "Melody 2" -> playShortMelody(
                listOf(
                    ToneGenerator.TONE_DTMF_1 to 160,
                    ToneGenerator.TONE_DTMF_5 to 160,
                    ToneGenerator.TONE_DTMF_9 to 180,
                    ToneGenerator.TONE_DTMF_5 to 240
                )
            )
            else -> {
                val soundRes = if (selected.contains("2")) R.raw.azan_2 else R.raw.azan_1
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
                if (player == null) stopSelf()
            }
        }

        // Foreground playback keeps running even when the activity is closed or
        // removed from Recent Apps. If Android kills the service, it may restart it.
        return START_STICKY
    }

    private fun playShortMelody(sequence: List<Pair<Int, Int>>) {
        val generator = ToneGenerator(AudioManager.STREAM_ALARM, 85)
        toneGenerator = generator

        var offset = 0L
        sequence.forEach { (tone, duration) ->
            handler.postDelayed({
                runCatching {
                    generator.stopTone()
                    generator.startTone(tone, duration)
                }
            }, offset)
            offset += duration + 90L
        }

        handler.postDelayed({
            runCatching { generator.stopTone() }
            runCatching { generator.release() }
            if (toneGenerator === generator) toneGenerator = null
            stopSelf()
        }, offset + 120L)
    }

    private fun stopCurrentPlayback() {
        handler.removeCallbacksAndMessages(null)

        player?.runCatching {
            if (isPlaying) stop()
            release()
        }
        player = null

        toneGenerator?.runCatching {
            stopTone()
            release()
        }
        toneGenerator = null
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
                description = "Azan and short melody playback"
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
