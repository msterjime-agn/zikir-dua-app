package com.msterjime.zikirdua

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// The widgets use the same saved city, language, method and calculation as the app.
// They never determine location themselves and never enable audio/notifications.
internal object PrayerWidgets {
    private const val PREFS = "zikir_dua_settings"
    private const val REQUEST = 880022
    private val zone = ZoneOffset.ofHours(5)
    private val providers = listOf(CompactPrayerWidget::class.java, FullPrayerWidget::class.java)
    private val clock = DateTimeFormatter.ofPattern("HH:mm")

    internal fun words(language: AppLanguage, tm: String, ru: String, en: String, tr: String): String = when (language) {
        AppLanguage.TM -> tm
        AppLanguage.RU -> ru
        AppLanguage.EN -> en
        AppLanguage.TR -> tr
    }

    internal fun language(context: Context): AppLanguage {
        val code = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("language", "tm")
        return AppLanguage.entries.firstOrNull { it.code == code } ?: AppLanguage.TM
    }

    private fun city(context: Context): City {
        val name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("city", "Köneürgenç")
        return Cities.firstOrNull { it.name == name } ?: Cities.first { it.name == "Köneürgenç" }
    }

    private data class Snapshot(
        val city: City, val lang: AppLanguage, val now: ZonedDateTime,
        val times: PrayerTimes, val nextIndex: Int, val next: ZonedDateTime,
        val nextName: String, val names: List<String>, val flags: Set<String>
    )

    private fun snapshot(context: Context): Snapshot {
        val city = city(context)
        val lang = language(context)
        val now = ZonedDateTime.now(zone)
        val times = calculatePrayerTimesWithContext(context, now.toLocalDate(), city)
        val p = prayerLabels(lang)
        val names = listOf(p.fajr, p.dhuhr, p.asr, p.maghrib, p.isha)
        val clocks = listOf(times.fajr, times.dhuhr, times.asr, times.maghrib, times.isha)
        val index = clocks.indexOfFirst { ZonedDateTime.of(now.toLocalDate(), it, zone).isAfter(now) }
        val next = if (index >= 0) ZonedDateTime.of(now.toLocalDate(), clocks[index], zone) else {
            val date = now.toLocalDate().plusDays(1)
            ZonedDateTime.of(date, calculatePrayerTimesWithContext(context, date, city).fajr, zone)
        }
        return Snapshot(city, lang, now, times, index, next, names[if (index >= 0) index else 0], names,
            flaggedPrayerScheduleKeys(context, now.toLocalDate(), city))
    }

    internal fun render(context: Context, compact: Boolean): RemoteViews {
        val data = snapshot(context)
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val light = p.getBoolean("widget_light", false)
        val ink = Color.parseColor(if (light) "#173F35" else "#FFFFFF")
        val secondary = Color.parseColor(if (light) "#476B60" else "#D1E4DB")
        val accent = Color.parseColor(if (light) "#846319" else "#E8CD85")
        val views = RemoteViews(context.packageName, if (compact) R.layout.widget_prayer_compact else R.layout.widget_prayer_full)
        views.setInt(R.id.widget_root, "setBackgroundResource", if (light) R.drawable.widget_prayer_light else R.drawable.widget_prayer_dark)
        views.setTextViewText(R.id.widget_city, data.city.name)
        views.setTextColor(R.id.widget_city, ink)
        views.setTextViewText(R.id.widget_next, data.nextName)
        views.setTextColor(R.id.widget_next, accent)
        views.setTextViewText(R.id.widget_time, data.next.format(clock))
        views.setTextColor(R.id.widget_time, ink)
        val tomorrow = data.next.toLocalDate() != data.now.toLocalDate()
        val nextWord = if (tomorrow) words(data.lang, "Ertir", "Завтра", "Tomorrow", "Yarın")
            else words(data.lang, "Indiki", "Следующий", "Next", "Sıradaki")
        val metadata = if (compact) nextWord + " · " + data.now.format(DateTimeFormatter.ofPattern("dd.MM")) else
            data.city.region + " · " + data.now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        views.setTextViewText(R.id.widget_meta, metadata)
        views.setTextColor(R.id.widget_meta, secondary)
        views.setTextColor(R.id.widget_refresh, accent)
        views.setContentDescription(R.id.widget_refresh, words(data.lang, "Täzele", "Обновить", "Refresh", "Yenile"))
        if (!compact) {
            val timeIds = listOf(R.id.widget_t0, R.id.widget_t1, R.id.widget_t2, R.id.widget_t3, R.id.widget_t4)
            val nameIds = listOf(R.id.widget_n0, R.id.widget_n1, R.id.widget_n2, R.id.widget_n3, R.id.widget_n4)
            val cellIds = listOf(R.id.widget_cell0, R.id.widget_cell1, R.id.widget_cell2, R.id.widget_cell3, R.id.widget_cell4)
            val keys = listOf("fajr_notification", "dhuhr_notification", "asr_notification", "maghrib_notification", "isha_notification")
            val values = listOf(data.times.fajr, data.times.dhuhr, data.times.asr, data.times.maghrib, data.times.isha)
            val shortNames = when (data.lang) {
                AppLanguage.TM -> listOf("Ertir", "Öýle", "Ikindi", "Agşam", "Ýassy")
                AppLanguage.RU -> listOf("Фаджр", "Зухр", "Аср", "Магриб", "Иша")
                AppLanguage.EN -> listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                AppLanguage.TR -> listOf("Sabah", "Öğle", "İkindi", "Akşam", "Yatsı")
            }
            values.forEachIndexed { i, value ->
                val active = i == data.nextIndex
                views.setTextViewText(timeIds[i], (if (keys[i] in data.flags) "⚠" else "") + value.format(clock))
                views.setTextViewText(nameIds[i], shortNames[i])
                views.setTextColor(timeIds[i], if (active) Color.parseColor("#173F35") else ink)
                views.setTextColor(nameIds[i], if (active) Color.parseColor("#173F35") else secondary)
                views.setInt(cellIds[i], "setBackgroundResource", if (active) R.drawable.widget_prayer_selected else R.drawable.widget_prayer_clear)
            }
            val sunrise = prayerLabels(data.lang).sunrise
            val mode = p.getString("prayer_calculation_mode", "MUFTIATE_TKM")
            val source = if (mode == "OFFLINE_BACKUP") words(data.lang, "Astronomiki", "Астрономический", "Astronomical", "Astronomik")
                else words(data.lang, "Tablisa + hasaplama", "Таблица + расчёт", "Table + calculation", "Tablo + hesap")
            views.setTextViewText(R.id.widget_footer, "$sunrise ${data.times.sunrise.format(clock)} · $source")
            views.setTextColor(R.id.widget_footer, secondary)
            views.setTextViewText(R.id.widget_hint, nextWord)
            views.setTextColor(R.id.widget_hint, secondary)
        }
        val open = PendingIntent.getActivity(context, REQUEST,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_root, open)
        val refresh = PendingIntent.getBroadcast(context, REQUEST + 1,
            Intent(context, CompactPrayerWidget::class.java).setAction(context.packageName + ".WIDGET_REFRESH"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_refresh, refresh)
        return views
    }

    fun updateAll(context: Context) {
        val app = context.applicationContext
        val manager = AppWidgetManager.getInstance(app)
        var installed = false
        providers.forEach { provider ->
            val ids = manager.getAppWidgetIds(ComponentName(app, provider))
            if (ids.isNotEmpty()) {
                installed = true
                runCatching { manager.updateAppWidget(ids, render(app, provider == CompactPrayerWidget::class.java)) }
                    .onFailure { android.util.Log.e("PrayerWidgets", "Widget update failed", it) }
            }
        }
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(app, CompactPrayerWidget::class.java).setAction(app.packageName + ".WIDGET_TICK")
        val pending = PendingIntent.getBroadcast(app, REQUEST + 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pending)
        if (!installed) return
        runCatching {
            val data = snapshot(app)
            val midnight = data.now.toLocalDate().plusDays(1).atStartOfDay(zone).plusSeconds(2)
            val event = if (data.next.isBefore(midnight)) data.next.plusSeconds(2) else midnight
            val millis = event.toInstant().toEpochMilli()
            if (Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()) {
                try { alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, millis, pending) }
                catch (_: SecurityException) { alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, millis, pending) }
            } else alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, millis, pending)
        }.onFailure { android.util.Log.e("PrayerWidgets", "Widget refresh scheduling failed", it) }
    }

    fun pin(context: Context, compact: Boolean) {
        val manager = AppWidgetManager.getInstance(context)
        if (manager.isRequestPinAppWidgetSupported) {
            val component = ComponentName(context, if (compact) CompactPrayerWidget::class.java else FullPrayerWidget::class.java)
            manager.requestPinAppWidget(component, null, null)
        } else {
            val lang = language(context)
            Toast.makeText(context, words(lang,
                "Baş ekrany uzak basyň → Widjetler → NAMAZ WAGTY 2.2",
                "Удерживайте рабочий стол → Виджеты → NAMAZ WAGTY 2.2",
                "Long-press the home screen → Widgets → NAMAZ WAGTY 2.2",
                "Ana ekrana uzun basın → Widget'lar → NAMAZ WAGTY 2.2"), Toast.LENGTH_LONG).show()
        }
    }
}

open class PrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = PrayerWidgets.updateAll(context)
    override fun onEnabled(context: Context) = PrayerWidgets.updateAll(context)
    override fun onDisabled(context: Context) = PrayerWidgets.updateAll(context)
    override fun onDeleted(context: Context, ids: IntArray) = PrayerWidgets.updateAll(context)
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) = PrayerWidgets.updateAll(context)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action.orEmpty()
        if (action == context.packageName + ".WIDGET_REFRESH" || action == context.packageName + ".WIDGET_TICK" ||
            action in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)) {
            PrayerWidgets.updateAll(context)
        }
    }
}
class CompactPrayerWidget : PrayerWidgetProvider()
class FullPrayerWidget : PrayerWidgetProvider()

@Composable
internal fun PrayerWidgetSettingsCard(language: AppLanguage) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }
    var light by remember { mutableStateOf(prefs.getBoolean("widget_light", false)) }
    fun t(tm: String, ru: String, en: String, tr: String) = PrayerWidgets.words(language, tm, ru, en, tr)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(t("Baş ekran widjeti", "Виджет на рабочем столе", "Home screen widget", "Ana ekran widget'ı"), style = MaterialTheme.typography.titleMedium)
        Text(t("Programmadaky şäher we hasaplama ulanylýar.", "Использует город и метод расчёта приложения.",
            "Uses the app's city and calculation method.", "Uygulamanın şehrini ve hesaplama yöntemini kullanır."), style = MaterialTheme.typography.bodySmall)
        Button(onClick = { PrayerWidgets.pin(context, true) }, modifier = Modifier.fillMaxWidth()) {
            Text(t("Kiçi widjet goş", "Добавить компактный виджет", "Add compact widget", "Küçük widget ekle"))
        }
        Button(onClick = { PrayerWidgets.pin(context, false) }, modifier = Modifier.fillMaxWidth()) {
            Text(t("Doly widjet goş", "Добавить виджет со всеми временами", "Add full timetable widget", "Tüm vakitler widget'ı ekle"))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(t("Açyk görnüş", "Светлое оформление", "Light appearance", "Açık görünüm"), modifier = Modifier.weight(1f))
            Switch(checked = light, onCheckedChange = {
                light = it
                prefs.edit().putBoolean("widget_light", it).apply()
                PrayerWidgets.updateAll(context)
            })
        }
        Text(t("Täzelenme wagty energiýa tygşytlama sazlamalaryna bagly bolup biler.",
            "Энергосбережение может задерживать обновление. Кнопка ↻ обновляет вручную.",
            "Battery saving may delay refresh. Tap ↻ to refresh manually.",
            "Pil tasarrufu yenilemeyi geciktirebilir. Elle yenilemek için ↻ düğmesine basın."),
            style = MaterialTheme.typography.bodySmall)
    }
}
