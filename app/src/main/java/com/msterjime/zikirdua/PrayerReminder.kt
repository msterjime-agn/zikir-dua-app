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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

private const val ReminderChannelId = "prayer_reminders"
private const val ReminderSchedulePrefs = "prayer_reminder_schedule"
private const val ReminderCodesKey = "request_codes"
private const val AppPrefs = "zikir_dua_settings"
private const val ReminderMinutesKey = "prayer_reminder_minutes"
private val ReminderZone = ZoneOffset.ofHours(5)

private val ReminderGreen = Color(0xFF2F6B57)
private val ReminderDeepGreen = Color(0xFF173F35)
private val ReminderSoftGreen = Color(0xFFEAF2EE)
private val ReminderGold = Color(0xFFC8A95B)

private data class ReminderStrings(
    val title: String,
    val subtitle: String,
    val off: String,
    val atTime: String,
    val notificationTitle: String
)

private fun reminderStrings(languageCode: String): ReminderStrings = when (languageCode) {
    "ru" -> ReminderStrings(
        "🔔 Напоминание о намазе",
        "Когда напомнить перед намазом",
        "Выкл.",
        "В момент",
        "Время намаза"
    )
    "en" -> ReminderStrings(
        "🔔 Prayer reminder",
        "Choose when to remind before prayer",
        "Off",
        "At time",
        "Prayer time"
    )
    "tr" -> ReminderStrings(
        "🔔 Namaz hatırlatıcısı",
        "Namazdan önce hatırlatma süresini seçin",
        "Kapalı",
        "Vaktinde",
        "Namaz vakti"
    )
    else -> ReminderStrings(
        "🔔 Namaz ýatlatmasy",
        "Namazdan öň duýduryş wagtyny saýlaň",
        "Öçük",
        "Wagtynda",
        "Namaz wagty"
    )
}

@Composable
internal fun PrayerReminderCard(
    city: City,
    labels: PrayerLabels,
    languageCode: String
) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences(AppPrefs, Context.MODE_PRIVATE) }
    var minutesBefore by remember {
        mutableIntStateOf(preferences.getInt(ReminderMinutesKey, -1))
    }
    val strings = reminderStrings(languageCode)
    val today = LocalDate.now(ReminderZone)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    fun selectReminder(value: Int) {
        minutesBefore = value
        preferences.edit().putInt(ReminderMinutesKey, value).apply()
        if (
            value >= 0 &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(city.name, languageCode, minutesBefore, today) {
        if (minutesBefore < 0) {
            cancelPrayerReminders(context)
        } else {
            schedulePrayerReminders(
                context = context,
                city = city,
                labels = labels,
                languageCode = languageCode,
                minutesBefore = minutesBefore,
                startDate = today
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                strings.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = ReminderDeepGreen
            )
            Text(strings.subtitle, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(listOf(-1, 0, 5, 10, 15, 30)) { option ->
                    val label = when {
                        option < 0 -> strings.off
                        option == 0 -> strings.atTime
                        else -> "$option min"
                    }
                    Button(
                        onClick = { selectReminder(option) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (minutesBefore == option) ReminderGold else ReminderSoftGreen,
                            contentColor = ReminderDeepGreen
                        )
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun buildReminderDays(
    city: City,
    labels: PrayerLabels,
    startDate: LocalDate,
    dayCount: Int = 14
): List<Pair<LocalDate, List<Pair<String, LocalTime>>>> = (0 until dayCount).map { offset ->
    val date = startDate.plusDays(offset.toLong())
    val times = calculatePrayerTimes(date, city)
    date to listOf(
        labels.fajr to times.fajr,
        labels.dhuhr to times.dhuhr,
        labels.asr to times.asr,
        labels.maghrib to times.maghrib,
        labels.isha to times.isha
    )
}

private fun schedulePrayerReminders(
    context: Context,
    city: City,
    labels: PrayerLabels,
    languageCode: String,
    minutesBefore: Int,
    startDate: LocalDate
) {
    cancelPrayerReminders(context)
    if (minutesBefore < 0) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val now = ZonedDateTime.now(ReminderZone)
    val requestCodes = mutableListOf<Int>()

    buildReminderDays(city, labels, startDate).forEach { (date, prayers) ->
        prayers.forEachIndexed { prayerIndex, (prayerName, prayerTime) ->
            val trigger = ZonedDateTime.of(date, prayerTime, ReminderZone)
                .minusMinutes(minutesBefore.toLong())
            if (!trigger.isAfter(now)) return@forEachIndexed

            val requestCode = ((date.toEpochDay() % 100000L) * 10L + prayerIndex).toInt()
            val intent = Intent(context, PrayerReminderReceiver::class.java).apply {
                putExtra("request_code", requestCode)
                putExtra("prayer_name", prayerName)
                putExtra("city_name", city.name)
                putExtra("minutes_before", minutesBefore)
                putExtra("language_code", languageCode)
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
            requestCodes += requestCode
        }
    }

    context.getSharedPreferences(ReminderSchedulePrefs, Context.MODE_PRIVATE)
        .edit()
        .putString(ReminderCodesKey, requestCodes.joinToString(","))
        .apply()
}

private fun cancelPrayerReminders(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val preferences = context.getSharedPreferences(ReminderSchedulePrefs, Context.MODE_PRIVATE)
    val requestCodes = preferences.getString(ReminderCodesKey, "").orEmpty()
        .split(',')
        .mapNotNull { it.toIntOrNull() }

    requestCodes.forEach { requestCode ->
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, PrayerReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    preferences.edit().remove(ReminderCodesKey).apply()
}

class PrayerReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        createReminderChannel(context)

        val requestCode = intent.getIntExtra("request_code", 1001)
        val prayerName = intent.getStringExtra("prayer_name").orEmpty()
        val cityName = intent.getStringExtra("city_name").orEmpty()
        val minutesBefore = intent.getIntExtra("minutes_before", 0)
        val languageCode = intent.getStringExtra("language_code") ?: "tm"
        val strings = reminderStrings(languageCode)

        val message = when (languageCode) {
            "ru" -> if (minutesBefore > 0) "$prayerName через $minutesBefore мин. • $cityName" else "$prayerName • $cityName"
            "en" -> if (minutesBefore > 0) "$prayerName in $minutesBefore min • $cityName" else "$prayerName • $cityName"
            "tr" -> if (minutesBefore > 0) "$prayerName için $minutesBefore dk kaldı • $cityName" else "$prayerName • $cityName"
            else -> if (minutesBefore > 0) "$prayerName wagtyna $minutesBefore minut galdy • $cityName" else "$prayerName • $cityName"
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
            .setContentTitle(strings.notificationTitle)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        NotificationManagerCompat.from(context).notify(requestCode, notification)
    }
}

class PrayerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val preferences = context.getSharedPreferences(AppPrefs, Context.MODE_PRIVATE)
        val minutesBefore = preferences.getInt(ReminderMinutesKey, -1)
        if (minutesBefore < 0) return

        val cityName = preferences.getString("city", "Köneürgenç") ?: "Köneürgenç"
        val city = Cities.firstOrNull { it.name == cityName } ?: return
        val languageCode = preferences.getString("language", "tm") ?: "tm"
        val language = AppLanguage.entries.firstOrNull { it.code == languageCode } ?: AppLanguage.TM

        schedulePrayerReminders(
            context = context,
            city = city,
            labels = prayerLabels(language),
            languageCode = languageCode,
            minutesBefore = minutesBefore,
            startDate = LocalDate.now(ReminderZone)
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
