package com.msterjime.zikirdua

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

private const val ReminderChannelId = "prayer_reminders"
private const val ReminderSchedulePrefs = "prayer_reminder_schedule"
private const val ReminderCodesKey = "request_codes"
private const val AppPrefs = "zikir_dua_settings"
private val ReminderZone = ZoneOffset.ofHours(5)

private val NotificationKeys = listOf(
    "fajr_notification",
    "dhuhr_notification",
    "asr_notification",
    "maghrib_notification",
    "isha_notification"
)

private data class ReceiverStrings(
    val prayerTime: String,
    val minutesLeft: (Int) -> String
)

private fun receiverStrings(languageCode: String): ReceiverStrings = when (languageCode) {
    "ru" -> ReceiverStrings(
        prayerTime = "Время намаза",
        minutesLeft = { minutes -> "До намаза осталось " + minutes + " мин." }
    )
    "en" -> ReceiverStrings(
        prayerTime = "Prayer time",
        minutesLeft = { minutes -> minutes.toString() + " min until prayer" }
    )
    "tr" -> ReceiverStrings(
        prayerTime = "Namaz vakti",
        minutesLeft = { minutes -> "Namaza " + minutes + " dk kaldı" }
    )
    else -> ReceiverStrings(
        prayerTime = "Namaz wagty",
        minutesLeft = { minutes -> "Namaza " + minutes + " minut galdy" }
    )
}

internal fun reschedulePrayerEvents(
    context: Context,
    city: City,
    labels: PrayerLabels,
    languageCode: String
) {
    cancelPrayerEvents(context)

    val preferences = context.getSharedPreferences(AppPrefs, Context.MODE_PRIVATE)
    val notificationEnabled = preferences.getBoolean("prayer_time_notification", false)
    val azanEnabled = preferences.getBoolean("azan_enabled", false)
    val reminderMinutes = preferences.getInt("reminder_minutes", 10)

    if (!notificationEnabled && !azanEnabled) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val now = ZonedDateTime.now(ReminderZone)
    val requestCodes = mutableListOf<Int>()

    repeat(14) { dayOffset ->
        val date = LocalDate.now(ReminderZone).plusDays(dayOffset.toLong())
        val times = calculatePrayerTimesWithContext(context, date, city)
        val prayers = listOf(
            labels.fajr to times.fajr,
            labels.dhuhr to times.dhuhr,
            labels.asr to times.asr,
            labels.maghrib to times.maghrib,
            labels.isha to times.isha
        )

        prayers.forEachIndexed { prayerIndex, (prayerName, prayerTime) ->
            if (!preferences.getBoolean(NotificationKeys[prayerIndex], true)) return@forEachIndexed

            val exactTime = ZonedDateTime.of(date, prayerTime, ReminderZone)
            if (exactTime.isAfter(now)) {
                val code = requestCode(date, prayerIndex, true)
                scheduleEvent(
                    context = context,
                    alarmManager = alarmManager,
                    requestCode = code,
                    trigger = exactTime,
                    prayerName = prayerName,
                    cityName = city.name,
                    languageCode = languageCode,
                    minutesBefore = 0,
                    exactPrayer = true
                )
                requestCodes += code
            }

            if (notificationEnabled && reminderMinutes > 0) {
                val reminderTime = exactTime.minusMinutes(reminderMinutes.toLong())
                if (reminderTime.isAfter(now)) {
                    val code = requestCode(date, prayerIndex, false)
                    scheduleEvent(
                        context = context,
                        alarmManager = alarmManager,
                        requestCode = code,
                        trigger = reminderTime,
                        prayerName = prayerName,
                        cityName = city.name,
                        languageCode = languageCode,
                        minutesBefore = reminderMinutes,
                        exactPrayer = false
                    )
                    requestCodes += code
                }
            }
        }
    }

    context.getSharedPreferences(ReminderSchedulePrefs, Context.MODE_PRIVATE)
        .edit()
        .putString(ReminderCodesKey, requestCodes.joinToString(","))
        .apply()
}

private fun requestCode(date: LocalDate, prayerIndex: Int, exactPrayer: Boolean): Int {
    val base = ((date.toEpochDay() % 100000L) * 100L).toInt()
    return base + prayerIndex * 2 + if (exactPrayer) 1 else 0
}

private fun scheduleEvent(
    context: Context,
    alarmManager: AlarmManager,
    requestCode: Int,
    trigger: ZonedDateTime,
    prayerName: String,
    cityName: String,
    languageCode: String,
    minutesBefore: Int,
    exactPrayer: Boolean
) {
    val intent = Intent(context, PrayerReminderReceiver::class.java).apply {
        putExtra("request_code", requestCode)
        putExtra("prayer_name", prayerName)
        putExtra("city_name", cityName)
        putExtra("language_code", languageCode)
        putExtra("minutes_before", minutesBefore)
        putExtra("exact_prayer", exactPrayer)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerMillis = trigger.toInstant().toEpochMilli()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
    } else {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
    }
}

private fun cancelPrayerEvents(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val schedulePrefs = context.getSharedPreferences(ReminderSchedulePrefs, Context.MODE_PRIVATE)
    val requestCodes = schedulePrefs.getString(ReminderCodesKey, "").orEmpty()
        .split(',')
        .mapNotNull { it.toIntOrNull() }

    requestCodes.forEach { code ->
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            code,
            Intent(context, PrayerReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    schedulePrefs.edit().remove(ReminderCodesKey).apply()
}

class PrayerReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val preferences = context.getSharedPreferences(AppPrefs, Context.MODE_PRIVATE)
        val exactPrayer = intent.getBooleanExtra("exact_prayer", false)
        val prayerName = intent.getStringExtra("prayer_name").orEmpty()
        val cityName = intent.getStringExtra("city_name").orEmpty()
        val languageCode = intent.getStringExtra("language_code") ?: "tm"
        val minutesBefore = intent.getIntExtra("minutes_before", 0)
        val requestCode = intent.getIntExtra("request_code", 1001)

        if (exactPrayer) {
            if (preferences.getBoolean("vibration_enabled", true)) {
                vibrateAtPrayer(context)
            }
            if (preferences.getBoolean("azan_enabled", false)) {
                runCatching { startAzanPlayback(context) }
            }
        }

        val shouldNotify = if (exactPrayer) {
            preferences.getBoolean("prayer_time_notification", false)
        } else {
            true
        }

        if (!shouldNotify) return
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        createReminderChannel(context)
        val strings = receiverStrings(languageCode)
        val message = if (exactPrayer || minutesBefore <= 0) {
            prayerName + " • " + cityName
        } else {
            strings.minutesLeft(minutesBefore) + " • " + prayerName + " • " + cityName
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(strings.prayerTime)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(requestCode, notification)
    }
}

class PrayerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val preferences = context.getSharedPreferences(AppPrefs, Context.MODE_PRIVATE)
        val cityName = preferences.getString("city", "Köneürgenç") ?: "Köneürgenç"
        val city = Cities.firstOrNull { it.name == cityName } ?: return
        val languageCode = preferences.getString("language", "tm") ?: "tm"
        val language = AppLanguage.entries.firstOrNull { it.code == languageCode } ?: AppLanguage.TM

        reschedulePrayerEvents(
            context = context,
            city = city,
            labels = prayerLabels(language),
            languageCode = languageCode
        )
    }
}

private fun vibrateAtPrayer(context: Context) {
    runCatching {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(
            VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }
}

private fun createReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            ReminderChannelId,
            "Namaz reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Prayer time reminders"
            enableVibration(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
