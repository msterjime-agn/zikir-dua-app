package com.msterjime.zikirdua

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

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

private enum class MelodyStyle {
    ARABIC,
    TURKISH
}

private data class MelodyNote(
    val midi: Int,
    val startSeconds: Double,
    val durationSeconds: Double,
    val amplitude: Double = 0.42
)

class AzanPlaybackService : Service() {
    private var player: MediaPlayer? = null
    private var melodyTrack: AudioTrack? = null
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
        var selected = preferences.getString("azan_sound", "Azan 1").orEmpty()

        // Migrate temporary tone names from previous builds.
        selected = when (selected) {
            "Melody 1" -> "Arabic Melody"
            "Melody 2" -> "Turkish Melody"
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

        when (selected) {
            "Arabic Melody" -> {
                if (melodyEnabled) playGeneratedMelody(MelodyStyle.ARABIC) else stopSelf()
            }
            "Turkish Melody" -> {
                if (melodyEnabled) playGeneratedMelody(MelodyStyle.TURKISH) else stopSelf()
            }
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

        // Foreground playback continues after the activity is closed or removed
        // from Recent Apps. Android may restart the service if the process is killed.
        return START_STICKY
    }

    private fun playGeneratedMelody(style: MelodyStyle) {
        val sampleRate = 22_050
        val pcm = synthesizeMelody(style, sampleRate)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()

        val written = track.write(pcm, 0, pcm.size)
        if (written <= 0) {
            track.release()
            stopSelf()
            return
        }

        melodyTrack = track
        track.play()

        val durationMs = pcm.size * 1000L / sampleRate + 350L
        handler.postDelayed({
            if (melodyTrack === track) {
                runCatching { track.stop() }
                runCatching { track.release() }
                melodyTrack = null
                stopSelf()
            }
        }, durationMs)
    }

    private fun synthesizeMelody(style: MelodyStyle, sampleRate: Int): ShortArray {
        val durationSeconds = if (style == MelodyStyle.ARABIC) 8.4 else 8.6
        val totalSamples = (durationSeconds * sampleRate).toInt()
        val mix = FloatArray(totalSamples)

        val notes = if (style == MelodyStyle.ARABIC) {
            listOf(
                MelodyNote(62, 0.00, 0.70, 0.46),
                MelodyNote(63, 0.65, 0.50),
                MelodyNote(66, 1.15, 0.70),
                MelodyNote(67, 1.80, 0.60),
                MelodyNote(69, 2.35, 0.80),
                MelodyNote(67, 3.05, 0.60),
                MelodyNote(66, 3.60, 0.80),
                MelodyNote(63, 4.30, 0.55),
                MelodyNote(62, 4.80, 1.00, 0.47),
                MelodyNote(69, 5.75, 0.48),
                MelodyNote(67, 6.20, 0.48),
                MelodyNote(66, 6.65, 0.55),
                MelodyNote(62, 7.15, 0.95, 0.47)
            )
        } else {
            listOf(
                MelodyNote(69, 0.00, 0.90, 0.38),
                MelodyNote(70, 0.78, 0.55, 0.36),
                MelodyNote(73, 1.28, 0.72, 0.38),
                MelodyNote(74, 1.92, 0.78, 0.39),
                MelodyNote(76, 2.62, 0.85, 0.40),
                MelodyNote(74, 3.38, 0.70, 0.38),
                MelodyNote(73, 4.02, 0.72, 0.37),
                MelodyNote(70, 4.67, 0.58, 0.36),
                MelodyNote(69, 5.18, 1.00, 0.39),
                MelodyNote(73, 6.08, 0.55, 0.37),
                MelodyNote(74, 6.56, 0.62, 0.38),
                MelodyNote(73, 7.10, 0.55, 0.36),
                MelodyNote(69, 7.60, 0.90, 0.39)
            )
        }

        notes.forEach { note ->
            if (style == MelodyStyle.ARABIC) {
                addOudLikeNote(mix, sampleRate, note)
            } else {
                addNeyLikeNote(mix, sampleRate, note)
            }
        }

        if (style == MelodyStyle.ARABIC) {
            addSoftDrone(mix, sampleRate, midiToFrequency(50), durationSeconds, 0.035)
        } else {
            listOf(
                MelodyNote(57, 0.00, 0.70, 0.14),
                MelodyNote(64, 2.55, 0.60, 0.14),
                MelodyNote(57, 5.15, 0.80, 0.14)
            ).forEach { addOudLikeNote(mix, sampleRate, it) }
        }

        addReverb(mix, sampleRate)
        return normalizeToPcm16(mix)
    }

    private fun addOudLikeNote(
        mix: FloatArray,
        sampleRate: Int,
        note: MelodyNote
    ) {
        val start = (note.startSeconds * sampleRate).toInt()
        val length = (note.durationSeconds * sampleRate).toInt()
        val frequency = midiToFrequency(note.midi)

        for (i in 0 until length) {
            val index = start + i
            if (index !in mix.indices) break

            val t = i.toDouble() / sampleRate
            val attack = 1.0 - kotlin.math.exp(-40.0 * t)
            val decay = kotlin.math.exp(-4.0 * t / note.durationSeconds)
            val envelope = attack * decay
            val phase = 2.0 * PI * frequency * t

            val sample =
                sin(phase) +
                    0.42 * sin(2.0 * phase + 0.20) +
                    0.18 * sin(3.0 * phase + 0.40)

            mix[index] += (note.amplitude * envelope * sample).toFloat()
        }
    }

    private fun addNeyLikeNote(
        mix: FloatArray,
        sampleRate: Int,
        note: MelodyNote
    ) {
        val start = (note.startSeconds * sampleRate).toInt()
        val length = (note.durationSeconds * sampleRate).toInt()
        val baseFrequency = midiToFrequency(note.midi)
        var phase = 0.0

        for (i in 0 until length) {
            val index = start + i
            if (index !in mix.indices) break

            val t = i.toDouble() / sampleRate
            val attack = (t / 0.12).coerceIn(0.0, 1.0)
            val release = ((note.durationSeconds - t) / 0.25).coerceIn(0.0, 1.0)
            val envelope = attack * release
            val vibrato = 1.0 + 0.003 * sin(2.0 * PI * 5.0 * t)
            phase += 2.0 * PI * baseFrequency * vibrato / sampleRate

            val sample =
                sin(phase) +
                    0.15 * sin(2.0 * phase + 0.20) +
                    0.05 * sin(3.0 * phase + 0.45)

            mix[index] += (note.amplitude * envelope * sample).toFloat()
        }
    }

    private fun addSoftDrone(
        mix: FloatArray,
        sampleRate: Int,
        frequency: Double,
        durationSeconds: Double,
        amplitude: Double
    ) {
        for (i in mix.indices) {
            val t = i.toDouble() / sampleRate
            val fadeIn = (t / 0.40).coerceIn(0.0, 1.0)
            val fadeOut = ((durationSeconds - t) / 0.70).coerceIn(0.0, 1.0)
            mix[i] += (
                amplitude *
                    fadeIn *
                    fadeOut *
                    sin(2.0 * PI * frequency * t)
                ).toFloat()
        }
    }

    private fun addReverb(mix: FloatArray, sampleRate: Int) {
        val dry = mix.copyOf()
        val delays = listOf(
            0.09 to 0.20,
            0.17 to 0.12,
            0.25 to 0.07
        )

        delays.forEach { (delaySeconds, gain) ->
            val delay = (delaySeconds * sampleRate).toInt()
            for (i in 0 until dry.size - delay) {
                mix[i + delay] += (dry[i] * gain).toFloat()
            }
        }
    }

    private fun normalizeToPcm16(mix: FloatArray): ShortArray {
        var peak = 0f
        mix.forEach { peak = max(peak, abs(it)) }
        val scale = if (peak > 0f) 0.88f / peak else 1f

        return ShortArray(mix.size) { index ->
            val value = (mix[index] * scale).coerceIn(-1f, 1f)
            (value * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun midiToFrequency(midi: Int): Double =
        440.0 * 2.0.pow((midi - 69) / 12.0)

    private fun stopCurrentPlayback() {
        handler.removeCallbacksAndMessages(null)

        player?.runCatching {
            if (isPlaying) stop()
            release()
        }
        player = null

        melodyTrack?.runCatching {
            stop()
            release()
        }
        melodyTrack = null
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
