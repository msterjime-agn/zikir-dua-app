package com.msterjime.zikirdua

import android.Manifest
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import androidx.core.app.NotificationCompat
import android.os.Build
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Vibrator
import android.os.VibrationEffect
import android.media.MediaPlayer
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

private val DeepGreen = Color(0xFF173F35)
private val Green = Color(0xFF2F6B57)
private val SoftGreen = Color(0xFFEAF2EE)
private val Gold = Color(0xFFC8A95B)
private val Ivory = Color(0xFFF8F6EF)
private val Ink = Color(0xFF1F2925)


private fun vibrateShort(context: Context) {
    runCatching {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(
            VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }
}

private fun vibrateComplete(context: Context) {
    runCatching {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(
            VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }
}



private fun playAzan(context: Context) {
    runCatching {
        val player = MediaPlayer.create(
            context,
            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
        )
        player?.setOnCompletionListener {
            it.release()
        }
        player?.start()
    }
}

private fun createPrayerNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "prayer_time",
            "Prayer time notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }
}


private fun showPrayerNotification(context: Context, title: String, message: String) {
    runCatching {
        createPrayerNotificationChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "prayer_time")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(title.hashCode(), notification)

        val preferences = context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
        if (preferences.getBoolean("azan_enabled", false)) {
            playAzan(context)
        }
    }
}


private fun checkPrayerReminder(
    context: Context,
    nextPrayer: NextPrayer,
    reminderMinutes: Int
) {
    runCatching {
        val now = ZonedDateTime.now(TurkmenistanZone)
        val prayerDateTime = ZonedDateTime.of(
            nextPrayer.date,
            nextPrayer.time,
            TurkmenistanZone
        )

        val minutesLeft = Duration.between(now, prayerDateTime).toMinutes()

        if (minutesLeft == reminderMinutes.toLong() && isPrayerNotificationEnabled(context, nextPrayer.name)) {
            showPrayerNotification(
                context,
                "🕌 ${nextPrayer.name}",
                "Через $reminderMinutes мин. наступает время намаза"
            )
        }
    }
}


private fun checkExactPrayerTime(
    context: Context,
    nextPrayer: NextPrayer
) {
    runCatching {
        val now = ZonedDateTime.now(TurkmenistanZone)
        val prayerDateTime = ZonedDateTime.of(
            nextPrayer.date,
            nextPrayer.time,
            TurkmenistanZone
        )

        val minutesLeft = Duration.between(now, prayerDateTime).toMinutes()

        if (minutesLeft == 0L && isPrayerNotificationEnabled(context, nextPrayer.name)) {
            showPrayerNotification(
                context,
                "🕌 ${nextPrayer.name}",
                "Наступило время намаза"
            )
        }
    }
}


private val PrayerNotificationKeys = listOf(
    "fajr_notification",
    "dhuhr_notification",
    "asr_notification",
    "maghrib_notification",
    "isha_notification"
)

private fun isPrayerNotificationEnabled(context: Context, prayerName: String): Boolean {
    val preferences = context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
    return when {
        prayerName.contains("ФАДЖР", true) || prayerName.contains("FAJR", true) || prayerName.contains("ERTIR", true) -> preferences.getBoolean("fajr_notification", true)
        prayerName.contains("ЗУХР", true) || prayerName.contains("DHUHR", true) || prayerName.contains("ÖÝLE", true) -> preferences.getBoolean("dhuhr_notification", true)
        prayerName.contains("АСР", true) || prayerName.contains("ASR", true) || prayerName.contains("IKINDI", true) -> preferences.getBoolean("asr_notification", true)
        prayerName.contains("МАГРИБ", true) || prayerName.contains("MAGHRIB", true) || prayerName.contains("AGŞAM", true) -> preferences.getBoolean("maghrib_notification", true)
        prayerName.contains("ИША", true) || prayerName.contains("ISHA", true) || prayerName.contains("ÝASSY", true) -> preferences.getBoolean("isha_notification", true)
        else -> true
    }
}


private enum class PrayerCalculationMode {
    MUFTIATE_TKM,
    OFFLINE_BACKUP
}


private fun getPrayerCalculationMode(context: Context): PrayerCalculationMode {
    val preferences = context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
    return if (preferences.getString("prayer_calculation_mode", "MUFTIATE_TKM")
        == "OFFLINE_BACKUP") {
        PrayerCalculationMode.OFFLINE_BACKUP
    } else {
        PrayerCalculationMode.MUFTIATE_TKM
    }
}

private fun savePrayerCalculationMode(
    context: Context,
    mode: PrayerCalculationMode
) {
    context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
        .edit()
        .putString("prayer_calculation_mode", mode.name)
        .apply()
}


private fun prayerCalculationModeLabel(context: Context): String {
    return when (getPrayerCalculationMode(context)) {
        PrayerCalculationMode.MUFTIATE_TKM -> "Муфтият ТКМ"
        PrayerCalculationMode.OFFLINE_BACKUP -> "Офлайн расчёт"
    }
}


private enum class AsrCalculationMethod {
    HANAFI,
    SHAFII
}

private fun getAsrCalculationMethod(context: Context): AsrCalculationMethod {
    val preferences = context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
    return if (preferences.getString("asr_method", "HANAFI") == "SHAFII") {
        AsrCalculationMethod.SHAFII
    } else {
        AsrCalculationMethod.HANAFI
    }
}

private fun saveAsrCalculationMethod(
    context: Context,
    method: AsrCalculationMethod
) {
    context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
        .edit()
        .putString("asr_method", method.name)
        .apply()
}


private data class PrayerCalculationParameters(
    val fajrAngle: Double,
    val ishaAngle: Double,
    val maghribAngle: Double,
    val description: String
)

private fun getPrayerCalculationParameters(context: Context): PrayerCalculationParameters {
    return when (getPrayerCalculationMode(context)) {
        PrayerCalculationMode.MUFTIATE_TKM -> PrayerCalculationParameters(
            fajrAngle = 18.0,
            ishaAngle = 17.0,
            maghribAngle = 0.833,
            description = "Муфтият ТКМ"
        )
        PrayerCalculationMode.OFFLINE_BACKUP -> PrayerCalculationParameters(
            fajrAngle = 18.0,
            ishaAngle = 17.0,
            maghribAngle = 0.833,
            description = "Офлайн"
        )
    }
}


private fun prayerParametersLabel(context: Context): String {
    val p = getPrayerCalculationParameters(context)
    return "Фаджр ${p.fajrAngle}° • Иша ${p.ishaAngle}° • ${p.description}"
}

private val AppColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Gold,
    background = Ivory,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink
)

internal data class City(
    val region: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

internal val Cities = listOf(
    City("Aşgabat", "Aşgabat", 37.9601, 58.3261),
    City("Arkadag", "Arkadag", 38.0550, 58.2000),

    City("Ahal", "Änew", 37.8875, 58.5160),
    City("Ahal", "Gökdepe", 38.1600, 57.9660),
    City("Ahal", "Bäherden", 38.4360, 57.4310),
    City("Ahal", "Tejen", 37.3833, 60.5000),
    City("Ahal", "Kaka", 37.3480, 59.6140),
    City("Ahal", "Sarahs", 36.5350, 61.2070),

    City("Balkan", "Balkanabat", 39.5108, 54.3671),
    City("Balkan", "Türkmenbaşy", 40.0230, 52.9690),
    City("Balkan", "Bereket", 39.2440, 55.5150),
    City("Balkan", "Gyzylarbat", 38.9750, 56.2770),
    City("Balkan", "Hazar", 39.4100, 53.1300),
    City("Balkan", "Esenguly", 37.4700, 53.9700),

    City("Daşoguz", "Daşoguz", 41.8363, 59.9666),
    City("Daşoguz", "Köneürgenç", 42.3271, 59.1545),
    City("Daşoguz", "Akdepe", 42.0550, 59.3780),
    City("Daşoguz", "Boldumsaz", 42.1280, 59.6710),
    City("Daşoguz", "Görogly", 41.6500, 59.9200),
    City("Daşoguz", "Şabat", 41.6500, 59.3600),
    City("Daşoguz", "Gubadag", 41.8300, 58.5700),
    City("Daşoguz", "Andalyp", 42.0000, 59.3000),

    City("Lebap", "Türkmenabat", 39.0733, 63.5787),
    City("Lebap", "Kerki", 37.8350, 65.2100),
    City("Lebap", "Köýtendag", 37.5000, 66.0000),
    City("Lebap", "Magdanly", 37.8130, 66.0000),
    City("Lebap", "Hojambaz", 38.0450, 64.9300),
    City("Lebap", "Farap", 39.1700, 63.6100),
    City("Lebap", "Döwletli", 37.9800, 65.7700),
    City("Lebap", "Garabekewül", 38.9440, 64.0800),
    City("Lebap", "Saýat", 38.7830, 63.8800),
    City("Lebap", "Darganata", 40.4700, 62.2800),

    City("Mary", "Mary", 37.5928, 61.8303),
    City("Mary", "Baýramaly", 37.6180, 62.1670),
    City("Mary", "Ýolöten", 37.2980, 62.3590),
    City("Mary", "Murgap", 37.4960, 61.9750),
    City("Mary", "Sakarçäge", 37.5830, 61.6500),
    City("Mary", "Wekilbazar", 37.7600, 62.0300),
    City("Mary", "Tagtabazar", 35.9530, 62.9130),
    City("Mary", "Serhetabat", 35.2790, 62.3430),
    City("Mary", "Parahat (Oguzhan)", 37.3000, 61.0000)
)

internal data class PrayerTimes(
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime
)

private data class NextPrayer(
    val name: String,
    val date: LocalDate,
    val time: LocalTime
)

private data class PrayerRow(val name: String, val time: LocalTime)

private val TurkmenistanZone = ZoneOffset.ofHours(5)
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun degreesToRadians(value: Double) = Math.toRadians(value)
private fun radiansToDegrees(value: Double) = Math.toDegrees(value)
private fun fixAngle(value: Double): Double = ((value % 360.0) + 360.0) % 360.0
private fun fixHour(value: Double): Double = ((value % 24.0) + 24.0) % 24.0

private fun julianDate(year: Int, month: Int, day: Int): Double {
    var y = year
    var m = month
    if (m <= 2) {
        y -= 1
        m += 12
    }
    val a = floor(y / 100.0)
    val b = 2 - a + floor(a / 4.0)
    return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
}

private fun sunPosition(jd: Double): Pair<Double, Double> {
    val d = jd - 2451545.0
    val g = fixAngle(357.529 + 0.98560028 * d)
    val q = fixAngle(280.459 + 0.98564736 * d)
    val l = fixAngle(q + 1.915 * sin(degreesToRadians(g)) + 0.020 * sin(degreesToRadians(2 * g)))
    val e = 23.439 - 0.00000036 * d
    val declination = radiansToDegrees(asin(sin(degreesToRadians(e)) * sin(degreesToRadians(l))))
    var rightAscension = radiansToDegrees(
        atan2(cos(degreesToRadians(e)) * sin(degreesToRadians(l)), cos(degreesToRadians(l)))
    ) / 15.0
    rightAscension = fixHour(rightAscension - q / 15.0) + q / 15.0
    val equation = q / 15.0 - rightAscension
    return declination to equation
}

private fun doubleHourToLocalTime(value: Double): LocalTime {
    val normalized = fixHour(value)
    var totalMinutes = (normalized * 60.0).roundToInt()
    totalMinutes %= 24 * 60
    if (totalMinutes < 0) totalMinutes += 24 * 60
    return LocalTime.of(totalMinutes / 60, totalMinutes % 60)
}



private fun calculateMuftiateTKMPrayerTimes(
    date: LocalDate,
    city: City
): PrayerTimes {
    // Подготовлено место для точной формулы Муфтията ТКМ.
    // До проверки официальной методики используется текущий стабильный расчёт.
    return calculatePrayerTimes(date, city)
}


internal fun calculatePrayerTimesWithParameters(
    context: Context,
    date: LocalDate,
    city: City
): PrayerTimes {
    return calculatePrayerTimes(date, city, context)
}

internal fun calculatePrayerTimesByMode(
    context: Context,
    date: LocalDate,
    city: City
): PrayerTimes {
    return when (getPrayerCalculationMode(context)) {
        PrayerCalculationMode.MUFTIATE_TKM -> {
            // Здесь будет подключён точный алгоритм Муфтията ТКМ.
            // Пока используется стабильный расчёт как резерв до замены формул.
            calculateMuftiateTKMPrayerTimes(date, city)
        }

        PrayerCalculationMode.OFFLINE_BACKUP -> {
            calculatePrayerTimes(date, city)
        }
    }
}

private fun asrFactor(context: Context): Double {
    return when (getAsrCalculationMethod(context)) {
        AsrCalculationMethod.HANAFI -> 2.0
        AsrCalculationMethod.SHAFII -> 1.0
    }
}

internal fun calculatePrayerTimesWithContext(
    context: Context,
    date: LocalDate,
    city: City
): PrayerTimes {
    val base = calculatePrayerTimes(date, city)

    // На этом этапе подключаем выбор Аср к расчёту.
    // Остальные времена сохраняются без изменений.
    return base.copy(
        asr = calculateAsrTime(
            context,
            date,
            city
        )
    )
}

private fun calculateAsrTime(
    context: Context,
    date: LocalDate,
    city: City
): LocalTime {
    val factor = asrFactor(context)

    // Расчёт Аср теперь использует выбранный метод:
    // Ханафи = тень в 2 раза
    // Шафи'и = тень в 1 раз
    return calculatePrayerTimes(
        date,
        city,
        context,
        asrFactorOverride = factor
    ).asr
}

internal fun calculatePrayerTimes(date: LocalDate, city: City, context: Context? = null, asrFactorOverride: Double? = null): PrayerTimes {
    val jDate = julianDate(date.year, date.monthValue, date.dayOfMonth) - city.longitude / (15.0 * 24.0)

    fun midDay(time: Double): Double {
        val equation = sunPosition(jDate + time).second
        return fixHour(12.0 - equation)
    }

    fun computeTime(angle: Double, time: Double): Double {
        val declination = sunPosition(jDate + time).first
        val noon = midDay(time)
        val numerator = -sin(degreesToRadians(angle)) -
            sin(degreesToRadians(declination)) * sin(degreesToRadians(city.latitude))
        val denominator = cos(degreesToRadians(declination)) * cos(degreesToRadians(city.latitude))
        val ratio = (numerator / denominator).coerceIn(-1.0, 1.0)
        val delta = radiansToDegrees(acos(ratio)) / 15.0
        return noon + if (angle > 90.0) -delta else delta
    }

    fun asrTime(factor: Double, time: Double): Double {
        val declination = sunPosition(jDate + time).first
        val angle = -radiansToDegrees(
            atan(1.0 / (factor + tan(degreesToRadians(abs(city.latitude - declination)))))
        )
        return computeTime(angle, time)
    }

    var fajr = 5.0
    var sunrise = 6.0
    var dhuhr = 12.0
    var asr = 13.0
    var maghrib = 18.0
    var isha = 18.0

    repeat(2) {
        fajr = computeTime(180.0 - getPrayerCalculationParameters(context).fajrAngle, fajr / 24.0)
        sunrise = computeTime(179.167, sunrise / 24.0)
        dhuhr = midDay(dhuhr / 24.0)
        asr = asrTime(asrFactorOverride ?: 1.0, asr / 24.0)
        maghrib = computeTime(getPrayerCalculationParameters(context).maghribAngle, maghrib / 24.0)
        isha = computeTime(getPrayerCalculationParameters(context).ishaAngle, isha / 24.0)
    }

    val offset = 5.0 - city.longitude / 15.0
    fajr += offset
    sunrise += offset
    dhuhr += offset
    asr += offset
    maghrib += offset
    isha += offset

    return PrayerTimes(
        doubleHourToLocalTime(fajr),
        doubleHourToLocalTime(sunrise),
        doubleHourToLocalTime(dhuhr),
        doubleHourToLocalTime(asr),
        doubleHourToLocalTime(maghrib),
        doubleHourToLocalTime(isha)
    )
}

internal enum class AppLanguage(val code: String, val label: String) {
    TM("tm", "🇹🇲 Türkmençe"),
    RU("ru", "🇷🇺 Русский"),
    EN("en", "🇬🇧 English"),
    TR("tr", "🇹🇷 Türkçe")
}

internal data class PrayerLabels(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

private data class UiText(
    val home: String,
    val prayerTimes: String,
    val dhikr: String,
    val tasbih: String,
    val nextPrayer: String,
    val timeLeft: String,
    val quickAccess: String,
    val prayerShortcut: String,
    val morningDhikr: String,
    val morningAfter: String,
    val eveningDhikr: String,
    val eveningAfter: String,
    val counter: String,
    val nextShort: String,
    val offlineNote: String,
    val dhikrDuaTitle: String,
    val chooseSection: String,
    val afterPrayer: String,
    val dhikrPrayers: String,
    val beforeSleep: String,
    val eveningPrayers: String,
    val personalPrayer: String,
    val savedPrayers: String,
    val target: String,
    val reset: String
)

internal fun prayerLabels(language: AppLanguage): PrayerLabels = when (language) {
    AppLanguage.TM -> PrayerLabels(
        "ERTIR NAMAZY", "GÜN DOGUŞY", "ÖÝLE NAMAZY",
        "IKINDI NAMAZY", "AGŞAM NAMAZY", "ÝASSY NAMAZY"
    )
    AppLanguage.RU -> PrayerLabels(
        "ФАДЖР", "ВОСХОД", "ЗУХР", "АСР", "МАГРИБ", "ИША"
    )
    AppLanguage.EN -> PrayerLabels(
        "FAJR", "SUNRISE", "DHUHR", "ASR", "MAGHRIB", "ISHA"
    )
    AppLanguage.TR -> PrayerLabels(
        "SABAH NAMAZI", "GÜNEŞ", "ÖĞLE NAMAZI",
        "İKİNDİ NAMAZI", "AKŞAM NAMAZI", "YATSI NAMAZI"
    )
}

private fun uiText(language: AppLanguage): UiText = when (language) {
    AppLanguage.TM -> UiText(
        home = "Baş sahypa",
        prayerTimes = "Namaz wagty",
        dhikr = "Zikr",
        tasbih = "Tesbih",
        nextPrayer = "Indiki namaz",
        timeLeft = "Galan wagt",
        quickAccess = "Çalt giriş",
        prayerShortcut = "ERTIR • ÖÝLE • IKINDI • AGŞAM • ÝASSY",
        morningDhikr = "Irdenki zikr",
        morningAfter = "Ertir namazyndan soň",
        eveningDhikr = "Agşamky zikr",
        eveningAfter = "Agşam namazyndan soň",
        counter = "Hasaplaýjy • 7 / 11 / 33 / 100 / 1000 / ∞",
        nextShort = "Indiki",
        offlineNote = "Häzir ätiýaçlyk oflaýn hasaplama görkezilýär: Ertir 18°, Ýassy 17°, UTC+5. Türkmenistanyň Müftüliginiň usuly esasy režim hökmünde indiki tapgyrda goşular.",
        dhikrDuaTitle = "Zikir & Dogalar",
        chooseSection = "Bölümi saýlaň",
        afterPrayer = "Namazdan soň",
        dhikrPrayers = "Zikir we dogalar",
        beforeSleep = "Ýatmazdan öň",
        eveningPrayers = "Agşamky dogalar",
        personalPrayer = "Şahsy doga",
        savedPrayers = "Ýatda saklanan dogalar",
        target = "Maksat",
        reset = "Nola düşür"
    )
    AppLanguage.RU -> UiText(
        home = "Главная",
        prayerTimes = "Время намаза",
        dhikr = "Зикр",
        tasbih = "Тасбих",
        nextPrayer = "Следующий намаз",
        timeLeft = "Осталось",
        quickAccess = "Быстрый доступ",
        prayerShortcut = "ФАДЖР • ЗУХР • АСР • МАГРИБ • ИША",
        morningDhikr = "Утренний зикр",
        morningAfter = "После Фаджра",
        eveningDhikr = "Вечерний зикр",
        eveningAfter = "После Магриба",
        counter = "Счётчик • 7 / 11 / 33 / 100 / 1000 / ∞",
        nextShort = "Следующий",
        offlineNote = "Сейчас используется резервный офлайн-расчёт: Фаджр 18°, Иша 17°, UTC+5. Метод Муфтията Туркменистана подключается через выбранный режим расчёта.",
        dhikrDuaTitle = "Зикр и дуа",
        chooseSection = "Выберите раздел",
        afterPrayer = "После намаза",
        dhikrPrayers = "Зикр и дуа",
        beforeSleep = "Перед сном",
        eveningPrayers = "Вечерние дуа",
        personalPrayer = "Личная дуа",
        savedPrayers = "Сохранённые дуа",
        target = "Цель",
        reset = "Сброс"
    )
    AppLanguage.EN -> UiText(
        home = "Home",
        prayerTimes = "Prayer times",
        dhikr = "Dhikr",
        tasbih = "Tasbih",
        nextPrayer = "Next prayer",
        timeLeft = "Time left",
        quickAccess = "Quick access",
        prayerShortcut = "FAJR • DHUHR • ASR • MAGHRIB • ISHA",
        morningDhikr = "Morning dhikr",
        morningAfter = "After Fajr",
        eveningDhikr = "Evening dhikr",
        eveningAfter = "After Maghrib",
        counter = "Counter • 7 / 11 / 33 / 100 / 1000 / ∞",
        nextShort = "Next",
        offlineNote = "A backup offline calculation is currently used: Fajr 18°, Isha 17°, UTC+5. The Turkmenistan Muftiate method will be added as the primary mode in the next stage.",
        dhikrDuaTitle = "Dhikr & Prayers",
        chooseSection = "Choose a section",
        afterPrayer = "After prayer",
        dhikrPrayers = "Dhikr and prayers",
        beforeSleep = "Before sleep",
        eveningPrayers = "Evening prayers",
        personalPrayer = "Personal prayer",
        savedPrayers = "Saved prayers",
        target = "Target",
        reset = "Reset"
    )
    AppLanguage.TR -> UiText(
        home = "Ana sayfa",
        prayerTimes = "Namaz vakitleri",
        dhikr = "Zikir",
        tasbih = "Tesbih",
        nextPrayer = "Sıradaki namaz",
        timeLeft = "Kalan süre",
        quickAccess = "Hızlı erişim",
        prayerShortcut = "SABAH • ÖĞLE • İKİNDİ • AKŞAM • YATSI",
        morningDhikr = "Sabah zikri",
        morningAfter = "Sabah namazından sonra",
        eveningDhikr = "Akşam zikri",
        eveningAfter = "Akşam namazından sonra",
        counter = "Sayaç • 7 / 11 / 33 / 100 / 1000 / ∞",
        nextShort = "Sıradaki",
        offlineNote = "Şu anda yedek çevrimdışı hesaplama kullanılıyor: Sabah 18°, Yatsı 17°, UTC+5. Türkmenistan Müftülüğü yöntemi bir sonraki aşamada ana yöntem olarak eklenecek.",
        dhikrDuaTitle = "Zikir & Dualar",
        chooseSection = "Bölüm seçin",
        afterPrayer = "Namazdan sonra",
        dhikrPrayers = "Zikir ve dualar",
        beforeSleep = "Uyumadan önce",
        eveningPrayers = "Akşam duaları",
        personalPrayer = "Kişisel dua",
        savedPrayers = "Kaydedilen dualar",
        target = "Hedef",
        reset = "Sıfırla"
    )
}

private fun regionLabel(city: City, language: AppLanguage): String = when (language) {
    AppLanguage.TM -> if (city.region == "Aşgabat" || city.region == "Arkadag") {
        "${city.region} şäheri"
    } else {
        "${city.region} welaýaty"
    }
    AppLanguage.RU -> if (city.region == "Aşgabat" || city.region == "Arkadag") {
        "г. ${city.region}"
    } else {
        "${city.region} велаят"
    }
    AppLanguage.EN -> if (city.region == "Aşgabat" || city.region == "Arkadag") {
        "${city.region} city"
    } else {
        "${city.region} Region"
    }
    AppLanguage.TR -> if (city.region == "Aşgabat" || city.region == "Arkadag") {
        "${city.region} şehri"
    } else {
        "${city.region} vilayeti"
    }
}

private fun findNextPrayer(
    now: ZonedDateTime,
    city: City,
    today: PrayerTimes,
    labels: PrayerLabels
): NextPrayer {
    val todayPrayers = listOf(
        labels.fajr to today.fajr,
        labels.dhuhr to today.dhuhr,
        labels.asr to today.asr,
        labels.maghrib to today.maghrib,
        labels.isha to today.isha
    )
    todayPrayers.forEach { (name, time) ->
        val candidate = ZonedDateTime.of(now.toLocalDate(), time, TurkmenistanZone)
        if (candidate.isAfter(now)) return NextPrayer(name, now.toLocalDate(), time)
    }
    val tomorrowDate = now.toLocalDate().plusDays(1)
    val tomorrow = calculatePrayerTimes(tomorrowDate, city)
    return NextPrayer(labels.fajr, tomorrowDate, tomorrow.fajr)
}

private fun countdownText(now: ZonedDateTime, next: NextPrayer): String {
    val nextDateTime = ZonedDateTime.of(next.date, next.time, TurkmenistanZone)
    val seconds = Duration.between(now, nextDateTime).seconds.coerceAtLeast(0)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, secs)
}

enum class AppTab(val symbol: String) {
    HOME("⌂"),
    DHIKR("✦"),
    TASBIH("●"),
    PRAYER("☾")
}

private fun notificationSettingsTitle(language: AppLanguage): String = when (language) {
    AppLanguage.TM -> "Bildiriş sazlamalary"
    AppLanguage.RU -> "Настройки уведомлений"
    AppLanguage.EN -> "Notification settings"
    AppLanguage.TR -> "Bildirim ayarları"
}

private fun notificationSettingsSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.TM -> "Ýatlatma • Azan • Bildiriş"
    AppLanguage.RU -> "Напоминание • Азан • Уведомления"
    AppLanguage.EN -> "Reminder • Adhan • Notifications"
    AppLanguage.TR -> "Hatırlatma • Ezan • Bildirimler"
}

private fun tabTitle(tab: AppTab, text: UiText, language: AppLanguage): String = when (tab) {
    AppTab.HOME -> text.home
    AppTab.PRAYER -> notificationSettingsTitle(language)
    AppTab.DHIKR -> text.dhikr
    AppTab.TASBIH -> text.tasbih
}

private fun nearestCity(latitude: Double, longitude: Double): City {
    return Cities.minByOrNull { city ->
        val result = FloatArray(1)
        Location.distanceBetween(latitude, longitude, city.latitude, city.longitude, result)
        result[0]
    } ?: Cities.first { it.name == "Köneürgenç" }
}

private fun detectNearestCity(context: Context, onCityDetected: (City) -> Unit) {
    val fineGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fineGranted && !coarseGranted) return

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = buildList {
        if (fineGranted && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            add(LocationManager.GPS_PROVIDER)
        }
        if ((fineGranted || coarseGranted) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            add(LocationManager.NETWORK_PROVIDER)
        }
    }
    if (providers.isEmpty()) return

    val lastLocation = providers
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { it.time }

    if (lastLocation != null) {
        onCityDetected(nearestCity(lastLocation.latitude, lastLocation.longitude))
        return
    }

    val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            onCityDetected(nearestCity(location.latitude, location.longitude))
        }

        @Deprecated("Deprecated in Android")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
    }

    runCatching {
        locationManager.requestSingleUpdate(providers.first(), listener, Looper.getMainLooper())
    }
}

private data class TasbihLabels(
    val chooseZikr: String,
    val changeZikr: String,
    val zikrs: String,
    val names99: String,
    val custom: String,
    val customHint: String,
    val unlimited: String,
    val save: String
)

private fun tasbihLabels(language: AppLanguage): TasbihLabels = when (language) {
    AppLanguage.TM -> TasbihLabels("Zikri saýla", "Zikri üýtget", "Zikrler", "Allanyň 99 ady", "Öz zikrim", "Zikriňizi ýazyň", "Çäksiz", "Ýatda sakla")
    AppLanguage.RU -> TasbihLabels("Выбрать зикр", "Изменить зикр", "Зикры", "99 имён Аллаха", "Свой зикр", "Введите свой зикр", "Без ограничений", "Сохранить")
    AppLanguage.EN -> TasbihLabels("Choose dhikr", "Change dhikr", "Dhikr", "99 Names of Allah", "My dhikr", "Enter your dhikr", "Unlimited", "Save")
    AppLanguage.TR -> TasbihLabels("Zikir seç", "Zikri değiştir", "Zikirler", "Allah'ın 99 ismi", "Kendi zikrim", "Zikrinizi yazın", "Sınırsız", "Kaydet")
}

private val PopularZikrs = listOf(
    "Subhanallah",
    "Alhamdulillah",
    "Allahu Akbar",
    "Astaghfirullah",
    "La ilaha illallah",
    "Subhanallahi wa bihamdihi",
    "Subhanallahil azim",
    "La hawla wa la quwwata illa billah",
    "Hasbunallahu wa ni'mal wakil",
    "Allahumma salli ala Muhammad",
    "La ilaha illallah wahdahu la sharika lah",
    "Subhanallahi wa bihamdihi adada khalqihi",
    "Ya Fattah",
    "Ya Razzaq",
    "Ya Ghaniyy",
    "Ya Mughni"
)

private val AllahNames99 = listOf(
    "Ar-Rahman", "Ar-Rahim", "Al-Malik", "Al-Quddus", "As-Salam", "Al-Mu'min",
    "Al-Muhaymin", "Al-Aziz", "Al-Jabbar", "Al-Mutakabbir", "Al-Khaliq", "Al-Bari'",
    "Al-Musawwir", "Al-Ghaffar", "Al-Qahhar", "Al-Wahhab", "Ar-Razzaq", "Al-Fattah",
    "Al-'Alim", "Al-Qabid", "Al-Basit", "Al-Khafid", "Ar-Rafi'", "Al-Mu'izz",
    "Al-Mudhill", "As-Sami'", "Al-Basir", "Al-Hakam", "Al-'Adl", "Al-Latif",
    "Al-Khabir", "Al-Halim", "Al-'Azim", "Al-Ghafur", "Ash-Shakur", "Al-'Aliyy",
    "Al-Kabir", "Al-Hafiz", "Al-Muqit", "Al-Hasib", "Al-Jalil", "Al-Karim",
    "Ar-Raqib", "Al-Mujib", "Al-Wasi'", "Al-Hakim", "Al-Wadud", "Al-Majid",
    "Al-Ba'ith", "Ash-Shahid", "Al-Haqq", "Al-Wakil", "Al-Qawiyy", "Al-Matin",
    "Al-Waliyy", "Al-Hamid", "Al-Muhsi", "Al-Mubdi'", "Al-Mu'id", "Al-Muhyi",
    "Al-Mumit", "Al-Hayy", "Al-Qayyum", "Al-Wajid", "Al-Maajid", "Al-Wahid",
    "Al-Ahad", "As-Samad", "Al-Qadir", "Al-Muqtadir", "Al-Muqaddim", "Al-Mu'akhkhir",
    "Al-Awwal", "Al-Akhir", "Az-Zahir", "Al-Batin", "Al-Waali", "Al-Muta'ali",
    "Al-Barr", "At-Tawwab", "Al-Muntaqim", "Al-'Afuww", "Ar-Ra'uf", "Malik-ul-Mulk",
    "Dhul-Jalali wal-Ikram", "Al-Muqsit", "Al-Jami'", "Al-Ghaniyy", "Al-Mughni", "Al-Mani'",
    "Ad-Darr", "An-Nafi'", "An-Nur", "Al-Hadi", "Al-Badi'", "Al-Baqi",
    "Al-Warith", "Ar-Rashid", "As-Sabur"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = Ivory) {
                    ZikirDuaApp()
                }
            }
        }
    }
}

@Composable
private fun ZikirDuaApp() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }
    val savedCityName = remember { preferences.getString("city", "Köneürgenç") ?: "Köneürgenç" }
    val savedLanguageCode = remember { preferences.getString("language", "tm") ?: "tm" }

    var selectedCity by remember {
        mutableStateOf(Cities.firstOrNull { it.name == savedCityName } ?: Cities.first { it.name == "Köneürgenç" })
    }
    var language by remember {
        mutableStateOf(AppLanguage.entries.firstOrNull { it.code == savedLanguageCode } ?: AppLanguage.TM)
    }
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var now by remember { mutableStateOf(ZonedDateTime.now(TurkmenistanZone)) }

    fun chooseCity(city: City) {
        selectedCity = city
        preferences.edit().putString("city", city.name).apply()
    }

    fun chooseLanguage(newLanguage: AppLanguage) {
        language = newLanguage
        preferences.edit().putString("language", newLanguage.code).apply()
    }

    fun refreshLocation() {
        detectNearestCity(context) { detectedCity -> chooseCity(detectedCity) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) refreshLocation()
    }

    fun requestLocation() {
    val fineGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (fineGranted || coarseGranted) {
        refreshLocation()
    } else {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

LaunchedEffect("auto_location") {
    requestLocation()
}

    val text = uiText(language)
    val prayerNames = prayerLabels(language)
    val prayerTimes = remember(selectedCity, now.toLocalDate()) {
        calculatePrayerTimesWithContext(context, now.toLocalDate(), selectedCity)
    }
    val nextPrayer = findNextPrayer(now, selectedCity, prayerTimes, prayerNames)
    val countdown = countdownText(now, nextPrayer)

    LaunchedEffect("clock") {
        while (true) {
            now = ZonedDateTime.now(TurkmenistanZone)

            val reminderMinutes = preferences.getInt("reminder_minutes", 10)
            val notificationEnabled = preferences.getBoolean("prayer_time_notification", false)

            if (notificationEnabled) {
                checkPrayerReminder(
                    context,
                    nextPrayer,
                    reminderMinutes
                )

                checkExactPrayerTime(
                    context,
                    nextPrayer
                )
            }

            delay(60000)
        }
    }

    BackHandler(enabled = selectedTab != AppTab.HOME) {
        selectedTab = AppTab.HOME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        containerColor = Ivory,
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding(), containerColor = Color.White) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Text(
                                tab.symbol,
                                fontSize = 22.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        label = { Text(tabTitle(tab, text, language), fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(
                    city = selectedCity,
                    prayerTimes = prayerTimes,
                    nextPrayer = nextPrayer,
                    countdown = countdown,
                    text = text,
                    language = language,
                    onLanguageSelected = ::chooseLanguage,
                    onAutoLocation = ::requestLocation,
                    onNotifications = { selectedTab = AppTab.PRAYER },
                    onDhikr = { selectedTab = AppTab.DHIKR },
                    onTasbih = { selectedTab = AppTab.TASBIH }
                )
                AppTab.PRAYER -> NotificationSettingsScreen(
                    city = selectedCity,
                    labels = prayerNames,
                    language = language
                )
                AppTab.DHIKR -> DhikrScreen(text)
                AppTab.TASBIH -> TasbihScreen(text, language)
            }
        }
    }
}

@Composable
private fun HomeScreen(
    city: City,
    prayerTimes: PrayerTimes,
    nextPrayer: NextPrayer,
    countdown: String,
    text: UiText,
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onAutoLocation: () -> Unit,
    onNotifications: () -> Unit,
    onDhikr: () -> Unit,
    onTasbih: () -> Unit
) {
    var languageMenuOpen by remember { mutableStateOf(false) }
    val labels = prayerLabels(language)

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F5F1), Ivory, Ivory)))
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text("NAMAZ WAGTY", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text("Zikir & Dogalar • v1.0", fontSize = 14.sp, color = Green)
            Box {
                Button(onClick = { languageMenuOpen = true }, colors = ButtonDefaults.buttonColors(containerColor = SoftGreen, contentColor = DeepGreen)) {
                    Text("🌐 ${language.label} ▾")
                }
                DropdownMenu(expanded = languageMenuOpen, onDismissRequest = { languageMenuOpen = false }) {
                    AppLanguage.entries.forEach { option ->
                        DropdownMenuItem(text = { Text(option.label) }, onClick = { onLanguageSelected(option); languageMenuOpen = false })
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = DeepGreen)) {
                Column(Modifier.padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("📍 ${city.name}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(regionLabel(city, language), color = Color.White.copy(alpha = .65f), fontSize = 12.sp)
                             Text(
                                 "🕌 ${prayerCalculationModeLabel(LocalContext.current)}",
                                 color = Gold,
                                 fontSize = 11.sp
                             )
                        }
                        Button(onClick = onAutoLocation, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepGreen)) { Text("GPS") }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("🕌 ${text.nextPrayer}", color = Color.White.copy(alpha=.75f), fontSize=13.sp)
                            Text(nextPrayer.name, color = Gold, fontSize=30.sp, fontWeight=FontWeight.Bold)
                            Text(nextPrayer.time.format(TimeFormatter), color=Color.White, fontSize=25.sp)
                            Text("${text.timeLeft}: $countdown", color=Color.White.copy(alpha=.75f), fontSize=13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${labels.fajr} ${prayerTimes.fajr.format(TimeFormatter)}", color=Color.White, fontSize=11.sp)
                            Text("${labels.dhuhr} ${prayerTimes.dhuhr.format(TimeFormatter)}", color=Color.White, fontSize=11.sp)
                            Text("${labels.asr} ${prayerTimes.asr.format(TimeFormatter)}", color=Color.White, fontSize=11.sp)
                            Text("${labels.maghrib} ${prayerTimes.maghrib.format(TimeFormatter)}", color=Color.White, fontSize=11.sp)
                            Text("${labels.isha} ${prayerTimes.isha.format(TimeFormatter)}", color=Color.White, fontSize=11.sp)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Text("🌅 ${labels.sunrise} ${prayerTimes.sunrise.format(TimeFormatter)}", color=Color.White.copy(alpha=.85f), fontSize=11.sp)
                        Text("🌇 ${labels.maghrib} ${prayerTimes.maghrib.format(TimeFormatter)}", color=Color.White.copy(alpha=.85f), fontSize=11.sp)
                    }
                }
            }
        }
        item { Text(text.quickAccess, fontSize=19.sp, fontWeight=FontWeight.SemiBold, color=Ink) }
        item { QuickAction("🔔", notificationSettingsTitle(language), notificationSettingsSubtitle(language), onNotifications) }
        item { QuickAction("✦", text.dhikr, text.dhikrDuaTitle, onDhikr) }
        item { QuickAction("●", text.tasbih, text.counter, onTasbih) }
    }
}

@Composable
private fun QuickAction(symbol: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(SoftGreen, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, fontSize = 24.sp, color = DeepGreen)
            }
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}


@Composable
private fun PrayerNotificationCard() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }

    var selectedMinutes by remember {
        mutableIntStateOf(preferences.getInt("reminder_minutes", 10))
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("🔔 Bildirişler", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
        Text("Öňünden duýdurmak", color = Green)

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(listOf(5, 10, 15, 30)) { minute ->
                Button(
                    onClick = {
                        selectedMinutes = minute
                        preferences.edit().putInt("reminder_minutes", minute).apply()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMinutes == minute) Gold else SoftGreen,
                        contentColor = DeepGreen
                    )
                ) {
                    Text("$minute min")
                }
            }
        }
    }
}

@Composable
private fun AzanSettingsCard() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }

    var azan by remember { mutableStateOf(preferences.getBoolean("azan_enabled", false)) }
    var vibration by remember { mutableStateOf(preferences.getBoolean("vibration_enabled", true)) }
    var selectedAzan by remember { mutableStateOf(preferences.getString("azan_sound", "Азан 1") ?: "Азан 1") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("📢 Azan sazlamalary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    azan = !azan
                    preferences.edit().putBoolean("azan_enabled", azan).apply()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (azan) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text(if (azan) "Azan ON" else "Azan OFF")
            }

            Button(
                onClick = {
                    vibration = !vibration
                    preferences.edit().putBoolean("vibration_enabled", vibration).apply()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vibration) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text(if (vibration) "Wibrasiýa ON" else "Wibrasiýa OFF")
            }
        }

        Text("Ses: $selectedAzan", color = Green)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Азан 1", "Азан 2", "Азан 3").forEach { sound ->
                Button(
                    onClick = {
                        selectedAzan = sound
                        preferences.edit().putString("azan_sound", sound).apply()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedAzan == sound) Gold else SoftGreen,
                        contentColor = DeepGreen
                    )
                ) {
                    Text(sound)
                }
            }
        }

        Button(
            onClick = {
                playAzan(context)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = SoftGreen,
                contentColor = DeepGreen
            )
        ) {
            Text("🔊 Test Azan")
        }
    }
}




@Composable
private fun AsrCalculationSettingsCard() {
    val context = LocalContext.current
    var method by remember { mutableStateOf(getAsrCalculationMethod(context)) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("🕌 Метод Аср", fontSize = 18.sp, color = DeepGreen)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    method = AsrCalculationMethod.HANAFI
                    saveAsrCalculationMethod(context, method)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (method == AsrCalculationMethod.HANAFI) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text("Ханафи")
            }

            Button(
                onClick = {
                    method = AsrCalculationMethod.SHAFII
                    saveAsrCalculationMethod(context, method)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (method == AsrCalculationMethod.SHAFII) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text("Шафи'и")
            }
        }
    }
}

@Composable
private fun PrayerCalculationSettingsCard() {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(getPrayerCalculationMode(context)) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("🕌 Метод расчёта времени намаза", fontSize = 18.sp, color = DeepGreen)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    mode = PrayerCalculationMode.MUFTIATE_TKM
                    savePrayerCalculationMode(context, mode)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == PrayerCalculationMode.MUFTIATE_TKM) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text("Муфтият ТКМ")
            }

            Button(
                onClick = {
                    mode = PrayerCalculationMode.OFFLINE_BACKUP
                    savePrayerCalculationMode(context, mode)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == PrayerCalculationMode.OFFLINE_BACKUP) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text("Офлайн")
            }
        }
    }
}


@Composable
private fun PrayerParametersCard() {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "📐 Параметры расчёта",
            fontSize = 18.sp,
            color = DeepGreen
        )

        Text(
            prayerParametersLabel(context),
            color = Green,
            fontSize = 14.sp
        )

        Text(
            "Параметры подготовлены для метода Муфтият ТКМ и резервного режима.",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun NotificationSettingsScreen(
    city: City,
    labels: PrayerLabels,
    language: AppLanguage
) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F5F1), Ivory, Ivory)))
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(10.dp))
            Text(
                notificationSettingsTitle(language),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DeepGreen
            )
            Text("📍 ${city.name}", color = Green)
        }

        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGreen)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrayerNotificationCard()
                    AzanSettingsCard()
                    PrayerCalculationSettingsCard()
                    PrayerParametersCard()
                    AsrCalculationSettingsCard()

                    var prayerTimeNotification by remember {
                        mutableStateOf(preferences.getBoolean("prayer_time_notification", false))
                    }

                    Text("🕌 Уведомление при наступлении времени намаза", color = DeepGreen)

                    val prayerNamesList = listOf(
                        "Фаджр",
                        "Зухр",
                        "Аср",
                        "Магриб",
                        "Иша"
                    )

                    prayerNamesList.forEachIndexed { index, prayerName ->
                        var enabled by remember {
                            mutableStateOf(
                                preferences.getBoolean(PrayerNotificationKeys[index], true)
                            )
                        }

                        Button(
                            onClick = {
                                enabled = !enabled
                                preferences.edit()
                                    .putBoolean(PrayerNotificationKeys[index], enabled)
                                    .apply()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (enabled) Gold else SoftGreen,
                                contentColor = DeepGreen
                            )
                        ) {
                            Text("$prayerName ${if (enabled) "ON" else "OFF"}")
                        }
                    }

                    Button(
                        onClick = {
                            prayerTimeNotification = !prayerTimeNotification
                            preferences.edit()
                                .putBoolean("prayer_time_notification", prayerTimeNotification)
                                .apply()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (prayerTimeNotification) Gold else SoftGreen,
                            contentColor = DeepGreen
                        )
                    ) {
                        Text(if (prayerTimeNotification) "ON" else "OFF")
                    }
                }
            }
        }
    }
}


@Composable
private fun DhikrScreen(text: UiText) {
    var selected by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }
    var counts by remember {
        mutableStateOf(
            mutableMapOf<Int, Int>().apply {
                for (i in 0..100) {
                    val saved = preferences.getInt("dhikr_count_$i", 0)
                    if (saved > 0) {
                        put(i, saved)
                    }
                }
            }
        )
    }

    fun saveDhikrCount(index: Int, value: Int) {
        preferences.edit().putInt("dhikr_count_$index", value).apply()
    }

    val categories = linkedMapOf(
        "☀ После Фаджра" to listOf(
            DhikrItem(
                "Аят аль-Курси ×1",
                "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
                "Allahu la ilaha illa huwa al-Hayyul-Qayyum",
                "Читать Аят аль-Курси.",
                "После Фаджра",
                "Поминание Аллаха и защита.",
                "Коран 2:255",
                1
            ),
            DhikrItem(
                "Утренний зикр ×3",
                "",
                "Subhanallahi wa bihamdihi, adada khalqihi...",
                "Пречист Аллах и хвала Ему.",
                "После Фаджра",
                "Прославление Аллаха.",
                "Хадис"
            ),
            DhikrItem(
                "Таухид ×100",
                "",
                "La ilaha illallahu wahdahu la sharika lah...",
                "Нет божества кроме Аллаха, у Которого нет сотоварища.",
                "Утром или в течение дня",
                "Таухид, поминание Аллаха и награда.",
                "Хадисы",
                100
            )
        ),
        "💼 Перед работой" to listOf(
            DhikrItem(
                "Дуа пророка Мусы (а.с.) об облегчении дела ×1",
                "",
                "Rabbi ishrah li sadri wa yassir li amri...",
                "Господи! Раскрой мою грудь, облегчи моё дело.",
                "Перед работой, разговором, экзаменом",
                "Облегчение дела, спокойствие и ясная речь.",
                "Коран 20:25–28"
            ),
            DhikrItem(
                "Для знаний ×1",
                "",
                "Rabbi zidni ilma",
                "Господи! Приумножь мои знания.",
                "Перед учёбой и интеллектуальной работой",
                "Полезные знания и понимание.",
                "Коран 20:114"
            )
        ),
        "🕌 После намаза" to listOf(
            DhikrItem(
                "Истигфар ×3",
                "أَسْتَغْفِرُ اللَّهَ",
                "Astaghfirullah",
                "Прошу Аллаха о прощении.",
                "После обязательного намаза",
                "Просьба о прощении.",
                "Сунна",
                3
            ),
            DhikrItem(
                "Субханаллах ×33",
                "",
                "Subhanallah",
                "Пречист Аллах.",
                "После намаза",
                "Прославление Аллаха.",
                "Сунна",
                33
            ),
            DhikrItem(
                "Альхамдулиллях ×33",
                "",
                "Alhamdulillah",
                "Хвала Аллаху.",
                "После намаза",
                "Благодарность Аллаху.",
                "Сунна",
                33
            ),
            DhikrItem(
                "Аллаху Акбар ×33",
                "",
                "Allahu Akbar",
                "Аллах Велик.",
                "После намаза",
                "Возвеличивание Аллаха.",
                "Сунна",
                33
            )
        ),
        "⚠ При трудностях" to listOf(
            DhikrItem(
                "Дуа пророка Юнуса (а.с.)",
                "",
                "La ilaha illa Anta subhanaka inni kuntu minaz-zalimin",
                "Нет божества кроме Тебя. Пречист Ты! Поистине, я был из числа несправедливых.",
                "При беде, тревоге и тяжёлой ситуации",
                "Обращение к Аллаху за избавлением от трудности.",
                "Коран 21:87"
            )
        ),
        "🌙 Перед сном" to listOf(
            DhikrItem(
                "Аят аль-Курси ×1",
                "",
                "Ayat al-Kursi",
                "Читать Аят аль-Курси.",
                "Перед сном",
                "Завершение дня и просьба о защите.",
                "Коран 2:255"
            )
        ),

        "❤️ За здоровье" to listOf(
            DhikrItem(
                "Дуа пророка Айюба (а.с.) при болезни ×1",
                "",
                "Anni massaniyad-durru wa Anta arhamur-rahimin",
                "Меня коснулась беда, а Ты — Милостивейший из милостивых.",
                "При болезни, слабости или боли",
                "Просьба об облегчении и исцелении.",
                "Коран 21:83"
            ),
            DhikrItem(
                "Дуа об исцелении ×1",
                "",
                "Allahumma Rabb an-nas, azhibil-ba's, ishfi Antash-Shafi",
                "О Аллах, Господь людей, удали болезнь и исцели.",
                "При болезни — для себя или другого человека",
                "Просьба об исцелении.",
                "Хадис"
            )
        ),
        "🌙 Вечером" to listOf(
            DhikrItem(
                "Дуа покаяния ×1",
                "",
                "Rabbana zalamna anfusana...",
                "Господь наш! Мы поступили несправедливо по отношению к самим себе.",
                "Вечером или после ошибки",
                "Покаяние и прощение.",
                "Коран 7:23"
            ),
            DhikrItem(
                "Дуа Мусы за себя и близких ×1",
                "",
                "Rabbighfir li wa li-akhi...",
                "Господи! Прости меня и моего брата.",
                "Вечером или при просьбе за близких",
                "Прощение и милость Аллаха.",
                "Коран 7:151"
            )
        )

    )

    if (selected == null) {
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(10.dp))
                Text(text.dhikrDuaTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text(text.dhikrPrayers, color = Green)
            }

            items(categories.keys.toList()) { category ->
                Card(
                    Modifier.fillMaxWidth().clickable { selected = category },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(category, Modifier.padding(18.dp), fontSize = 19.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                }
            }
        }
    } else {
        val list = categories[selected] ?: emptyList()

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(onClick = { selected = null }) { Text("← Назад") }
                Text(selected ?: "", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            }

            items(list.size) { index ->
                val item = list[index]
                val value = counts[index] ?: 0

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                        if (item.arabic.isNotBlank()) Text(item.arabic, color = Gold, fontSize = 22.sp)
                        Text(item.transliteration, color = Green)
                        Text(item.translation, color = Color.Gray)

                        if (item.whenToRead.isNotBlank()) {
                            Text("Когда: ${item.whenToRead}", color = Green)
                        }
                        if (item.purpose.isNotBlank()) {
                            Text("Цель: ${item.purpose}", color = Green)
                        }
                        if (item.source.isNotBlank()) {
                            Text("Источник: ${item.source}", color = Gold)
                        }

                        Button(onClick = {
                            val next = value + 1
                            counts[index] = if (next >= item.countTarget) 0 else next
                            saveDhikrCount(index, counts[index] ?: 0)
                            counts = counts.toMutableMap()
                            if (next >= item.countTarget) {
                                vibrateComplete(context)
                            } else {
                                vibrateShort(context)
                            }
                        }) {
                            Text("$value / ${item.countTarget}  +1")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TasbihScreen(text: UiText, language: AppLanguage) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }
    val labels = tasbihLabels(language)

    var count by remember { mutableIntStateOf(preferences.getInt("tasbih_count", 0)) }
    var target by remember { mutableIntStateOf(preferences.getInt("tasbih_target", 33)) }
    var selectedZikr by remember { mutableStateOf(preferences.getString("tasbih_zikr", "") ?: "") }
    var chooserOpen by remember { mutableStateOf(false) }
    var chooserSection by remember { mutableIntStateOf(0) }
    var customDraft by remember { mutableStateOf("") }

    val targetText = if (target < 0) labels.unlimited else target.toString()
    val targetOptions = listOf(7, 11, 33, 100, 1000, -1)

    fun saveCount(value: Int) {
        count = value
        preferences.edit().putInt("tasbih_count", value).apply()
    }

    fun saveTarget(value: Int) {
        target = value
        preferences.edit().putInt("tasbih_target", value).apply()
    }

    fun saveZikr(value: String) {
        selectedZikr = value
        preferences.edit().putString("tasbih_zikr", value).apply()
    }

    if (chooserOpen) {
        AlertDialog(
            onDismissRequest = { chooserOpen = false },
            title = { Text(labels.chooseZikr) },
            text = {
                Column {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Button(onClick = { chooserSection = 0 }) { Text(labels.zikrs) }
                        }
                        item {
                            Button(onClick = { chooserSection = 1 }) { Text(labels.names99) }
                        }
                        item {
                            Button(onClick = { chooserSection = 2 }) { Text(labels.custom) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    when (chooserSection) {
                        0 -> LazyColumn(Modifier.height(340.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(PopularZikrs) { zikr ->
                                Card(
                                    Modifier.fillMaxWidth().clickable {
                                        saveZikr(zikr)
                                        chooserOpen = false
                                    },
                                    colors = CardDefaults.cardColors(containerColor = SoftGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(zikr, Modifier.padding(12.dp), color = DeepGreen, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        1 -> LazyColumn(Modifier.height(340.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(AllahNames99) { name ->
                                Card(
                                    Modifier.fillMaxWidth().clickable {
                                        saveZikr(name)
                                        chooserOpen = false
                                    },
                                    colors = CardDefaults.cardColors(containerColor = SoftGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(name, Modifier.padding(12.dp), color = DeepGreen, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        else -> Column {
                            OutlinedTextField(
                                value = customDraft,
                                onValueChange = { customDraft = it },
                                label = { Text(labels.customHint) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (customDraft.isNotBlank()) {
                                        saveZikr(customDraft.trim())
                                        chooserOpen = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(labels.save)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(6.dp))
        Text(text.tasbih, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
        Text("${text.target}: $targetText", color = Green)
        Spacer(Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(targetOptions) { option ->
                val optionLabel = if (option < 0) "∞" else option.toString()
                Button(
                    onClick = { saveTarget(option) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (target == option) Gold else SoftGreen,
                        contentColor = DeepGreen
                    )
                ) {
                    Text(optionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { saveCount(0) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Green)
        ) {
            Text(text.reset)
        }

        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DeepGreen)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(top = 30.dp, bottom = 22.dp, start = 22.dp, end = 22.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$count", fontSize = 68.sp, fontWeight = FontWeight.Bold, color = Gold)
                    if (target >= 0) {
                        Text("/ $target", color = Color.White.copy(alpha = 0.75f))
                    } else {
                        Text("∞", color = Color.White.copy(alpha = 0.75f), fontSize = 22.sp)
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedZikr.isBlank()) {
                        Button(
                            onClick = {
                                customDraft = ""
                                chooserOpen = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DeepGreen)
                        ) {
                            Text(labels.chooseZikr)
                        }
                    } else {
                        Text(
                            selectedZikr,
                            color = Gold,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                customDraft = selectedZikr
                                chooserOpen = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DeepGreen)
                        ) {
                            Text(labels.changeZikr)
                        }
                    }
                }

                Button(
                    onClick = {
                        val next = count + 1
                        vibrateShort(context)
                        if (target > 0 && next >= target) {
                            vibrateComplete(context)
                            saveCount(0)
                        } else {
                            saveCount(next)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).size(120.dp),
                    shape = RoundedCornerShape(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepGreen)
                ) {
                    Text("+", fontSize = 42.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// ===== Dhikr detail screens =====

private data class DhikrItem(
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val whenToRead: String = "",
    val purpose: String = "",
    val source: String = "",
    val countTarget: Int = 1
)

private val MorningDhikr = listOf(
    DhikrItem(
        "Ayat al-Kursi",
        "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
        "Allahu la ilaha illa huwa al-Hayyul-Qayyum",
        "Аллах — нет божества кроме Него, Живого и Вечно Сущего"
    ),
    DhikrItem(
        "Subhanallahi wa bihamdihi ×100",
        "",
        "Subhanallahi wa bihamdihi",
        "Пречист Аллах и Ему хвала"
    )
)

@Composable
private fun DhikrDetailScreen(
    title: String,
    items: List<DhikrItem>
) {
    var completed by remember { mutableStateOf(setOf<Int>()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DeepGreen
            )
        }

        items(items.size) { index ->
            val item = items[index]

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        item.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepGreen
                    )

                    Spacer(Modifier.height(8.dp))

                    if (item.arabic.isNotBlank()) {
                        Text(item.arabic, fontSize = 22.sp, color = Gold)
                    }

                    Text(item.transliteration, color = Green)
                    Text(item.translation, color = Color.Gray)

                    Button(
                        onClick = {
                            completed =
                                if (index in completed)
                                    completed - index
                                else
                                    completed + index
                        }
                    ) {
                        Text(
                            if (index in completed)
                                "✓ Прочитано"
                            else
                                "○ Отметить"
                        )
                    }
                }
            }
        }
    }
}
