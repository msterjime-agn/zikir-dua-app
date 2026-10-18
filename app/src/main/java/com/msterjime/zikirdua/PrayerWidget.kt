package com.msterjime.zikirdua

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateAll(context)
    }

    companion object {
        private val zone = ZoneOffset.ofHours(5)
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PrayerWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val preferences = context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
            val cityName = preferences.getString("city", "Köneürgenç") ?: "Köneürgenç"
            val languageCode = preferences.getString("language", "tm") ?: "tm"
            val language = AppLanguage.entries.firstOrNull { it.code == languageCode } ?: AppLanguage.TM
            val city = Cities.firstOrNull { it.name == cityName } ?: Cities.first()

            val now = ZonedDateTime.now(zone)
            val today = now.toLocalDate()
            val times = calculatePrayerTimesWithContext(context, today, city)
            val labels = prayerLabels(language)
            val next = nextPrayer(context, now, city, times, labels)

            val views = RemoteViews(context.packageName, R.layout.widget_prayer)
            views.setTextViewText(R.id.widget_city, "📍 ${city.name}")
            views.setTextViewText(
                R.id.widget_next_label,
                when (language) {
                    AppLanguage.TM -> "INDIKI NAMAZ"
                    AppLanguage.RU -> "СЛЕДУЮЩИЙ НАМАЗ"
                    AppLanguage.EN -> "NEXT PRAYER"
                    AppLanguage.TR -> "SONRAKİ NAMAZ"
                }
            )
            views.setTextViewText(R.id.widget_next_name, next.first)
            views.setTextViewText(R.id.widget_next_time, next.second.format(timeFormatter))

            views.setTextViewText(
                R.id.widget_prayer_row_1,
                "${shortLabel(labels.fajr)} ${times.fajr.format(timeFormatter)}   •   " +
                    "${shortLabel(labels.dhuhr)} ${times.dhuhr.format(timeFormatter)}   •   " +
                    "${shortLabel(labels.asr)} ${times.asr.format(timeFormatter)}"
            )
            views.setTextViewText(
                R.id.widget_prayer_row_2,
                "${shortLabel(labels.maghrib)} ${times.maghrib.format(timeFormatter)}   •   " +
                    "${shortLabel(labels.isha)} ${times.isha.format(timeFormatter)}"
            )

            val openIntent = Intent(context, MainActivity::class.java)
            val openPendingIntent = PendingIntent.getActivity(
                context,
                94001,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openPendingIntent)

            manager.updateAppWidget(appWidgetId, views)
        }

        private fun nextPrayer(
            context: Context,
            now: ZonedDateTime,
            city: City,
            today: PrayerTimes,
            labels: PrayerLabels
        ): Pair<String, LocalTime> {
            val prayers = listOf(
                labels.fajr to today.fajr,
                labels.dhuhr to today.dhuhr,
                labels.asr to today.asr,
                labels.maghrib to today.maghrib,
                labels.isha to today.isha
            )
            prayers.forEach { (name, time) ->
                val moment = ZonedDateTime.of(now.toLocalDate(), time, zone)
                if (moment.isAfter(now)) return name to time
            }

            val tomorrow: LocalDate = now.toLocalDate().plusDays(1)
            val tomorrowTimes = calculatePrayerTimesWithContext(context, tomorrow, city)
            return labels.fajr to tomorrowTimes.fajr
        }

        private fun shortLabel(label: String): String =
            label
                .replace(" NAMAZY", "")
                .replace(" NAMAZI", "")
                .replace(" NAMAZ", "")
                .replace("PRAYER", "")
                .trim()
    }
}
