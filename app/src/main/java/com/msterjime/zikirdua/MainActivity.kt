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

private fun localized(
    language: AppLanguage,
    tm: String,
    ru: String,
    en: String,
    tr: String
): String = when (language) {
    AppLanguage.TM -> tm
    AppLanguage.RU -> ru
    AppLanguage.EN -> en
    AppLanguage.TR -> tr
}

private fun dhikrReading(text: String, language: AppLanguage): String {
    if (language != AppLanguage.TM) return text

    return when (text.trim()) {
        "Allahu la ilaha illa Huwa, Al-Hayyul-Qayyum. La ta'khudhuhu sinatun wa la nawm. Lahu ma fis-samawati wa ma fil-ard. Man dhal-ladhi yashfa'u 'indahu illa bi-idhnih. Ya'lamu ma bayna aydihim wa ma khalfahum. Wa la yuhituna bi-shay'in min 'ilmihi illa bima sha'. Wasi'a kursiyyuhus-samawati wal-ard. Wa la ya'uduhu hifzuhuma. Wa Huwal-'Aliyyul-'Azim." ->
            "Allahu lä ilähe illä Huwa, Al-Haýýul-Kaýýum. Lä tä'huzuhu sinätun wä lä näwm. Lahu mä fis-samawäti wä mä fil-arz. Men zäl-läzi ýeşfe'u 'indahu illä bi-iznih. Ýa'lamu mä baýna aýdihim wä mä halfahum. Wä lä ýuhituna bi-şeý'in min 'ilmihi illä bimä şa'. Wasi'a kursiýýuhus-samawäti wal-arz. Wä lä ýa'uduhu hifzuhumä. Wä Huwal-'Aliýýul-'Azim."
        "Subhanallahi wa bihamdihi, 'adada khalqihi, wa rida nafsihi, wa zinata 'arshihi, wa midada kalimatihi." ->
            "Subhanallahi wä bihamdihi, 'adada halkihi, wä rida nafsihi, wä zinata 'arşihi, wä midada kalimatihi."
        "La ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa huwa 'ala kulli shay'in qadir." ->
            "Lä ilähe illallahu wahdahu lä şärikä lah, lahul-mulku wä lahul-hamdu wä huwa 'alä kulli şeý'in kadir."
        "Rabbi ishrah li sadri, wa yassir li amri, wahlul 'uqdatan min lisani, yafqahu qawli." ->
            "Rabbi işrah li sadri, wä ýassir li amri, wahlul 'ukdatan min lisäni, ýafkahu kawli."
        "Rabbi zidni 'ilma." ->
            "Rabbi zidni 'ilmä."
        "Astaghfirullah." ->
            "Astaghfirullah."
        "Allahumma Antas-Salamu wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram." ->
            "Allahumma Antas-Salämu wä minkas-saläm, tabärakta ýa Zal-Jaläli wal-Ikram."
        "Subhanallah." ->
            "Subhanallah."
        "Alhamdulillah." ->
            "Alhamdulillah."
        "Allahu Akbar." ->
            "Allahu Akbar."
        "La ilaha illa Anta subhanaka inni kuntu minaz-zalimin." ->
            "Lä ilähe illä Anta subhanaka inni kuntu minaz-zalimin."
        "Anni massaniyad-durru wa Anta arhamur-rahimin." ->
            "Anni massaniýad-durru wä Anta arhamur-rahimin."
        "Allahumma Rabb an-nas, adhhib al-ba's, ishfi Antash-Shafi, la shifa'a illa shifa'uk, shifa'an la yughadiru saqama." ->
            "Allahumma Rabb an-näs, azhib al-ba's, işfi Antaş-Şäfi, lä şifä'a illä şifä'uk, şifä'an lä ýugadiru sakama."
        "Rabbana zalamna anfusana wa in lam taghfir lana wa tarhamna lanakunanna minal-khasirin." ->
            "Rabbanä zalamnä anfusanä wä in lam taghfir lanä wä tarhamnä lanakunnanna minal-häsirin."
        "Rabbighfir li wa li-akhi wa adkhilna fi rahmatika wa Anta arhamur-rahimin." ->
            "Rabbighfir li wä li-ahi wä adhilnä fi rahmatika wä Anta arhamur-rahimin."
        else -> text
            .replace("sh", "ş", ignoreCase = true)
            .replace("kh", "h", ignoreCase = true)
            .replace(" q", " k", ignoreCase = true)
            .replace("wa ", "wä ", ignoreCase = true)
            .replace(" la ", " lä ", ignoreCase = true)
    }
}


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
    runCatching { startAzanPlayback(context) }
}

private fun playTasbihClick(context: Context) {
    val preferences = context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
    if (!preferences.getBoolean("tasbih_click_enabled", true)) return

    runCatching {
        val player = MediaPlayer.create(context, R.raw.tasbih_soft_click)
        player?.setVolume(0.45f, 0.45f)
        player?.setOnCompletionListener { it.release() }
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


private fun prayerCalculationModeLabel(context: Context, language: AppLanguage): String {
    return when (getPrayerCalculationMode(context)) {
        PrayerCalculationMode.MUFTIATE_TKM ->
            localized(language, "Müftülik TKM", "Муфтият ТКМ", "Muftiate TKM", "Müftülük TKM")
        PrayerCalculationMode.OFFLINE_BACKUP ->
            localized(language, "Oflaýn hasaplama", "Офлайн расчёт", "Offline calculation", "Çevrimdışı hesaplama")
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


private fun prayerParametersLabel(context: Context, language: AppLanguage): String {
    return when (getPrayerCalculationMode(context)) {
        PrayerCalculationMode.MUFTIATE_TKM -> localized(
            language,
            "Takyk Müftülik tertibi • sebit boýunça tablisa",
            "Точное расписание Муфтията • таблица по региону",
            "Exact Muftiate timetable • regional table",
            "Kesin Müftülük takvimi • bölgesel tablo"
        )
        PrayerCalculationMode.OFFLINE_BACKUP -> {
            val p = getPrayerCalculationParameters(context)
            localized(
                language,
                "Ertir ${p.fajrAngle}° • Ýassy ${p.ishaAngle}° • Oflaýn",
                "Фаджр ${p.fajrAngle}° • Иша ${p.ishaAngle}° • Офлайн",
                "Fajr ${p.fajrAngle}° • Isha ${p.ishaAngle}° • Offline",
                "Sabah ${p.fajrAngle}° • Yatsı ${p.ishaAngle}° • Çevrimdışı"
            )
        }
    }
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
    return MuftiateSchedule.prayerTimes(date, city)
        ?: calculatePrayerTimes(date, city)
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
        PrayerCalculationMode.MUFTIATE_TKM ->
            calculateMuftiateTKMPrayerTimes(date, city)
        PrayerCalculationMode.OFFLINE_BACKUP ->
            calculatePrayerTimes(date, city, context)
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
    return when (getPrayerCalculationMode(context)) {
        PrayerCalculationMode.MUFTIATE_TKM ->
            calculateMuftiateTKMPrayerTimes(date, city)

        PrayerCalculationMode.OFFLINE_BACKUP -> {
            val base = calculatePrayerTimes(date, city, context)
            base.copy(
                asr = calculateAsrTime(
                    context,
                    date,
                    city
                )
            )
        }
    }
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
    val parameters = if (context != null) {
        getPrayerCalculationParameters(context)
    } else {
        PrayerCalculationParameters(
            fajrAngle = 18.0,
            ishaAngle = 17.0,
            maghribAngle = 0.833,
            description = "default"
        )
    }
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
        fajr = computeTime(180.0 - parameters.fajrAngle, fajr / 24.0)
        sunrise = computeTime(179.167, sunrise / 24.0)
        dhuhr = midDay(dhuhr / 24.0)
        asr = asrTime(asrFactorOverride ?: 1.0, asr / 24.0)
        maghrib = computeTime(parameters.maghribAngle, maghrib / 24.0)
        isha = computeTime(parameters.ishaAngle, isha / 24.0)
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
        dhikrDuaTitle = "Zikir we dogalar",
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
    context: Context,
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
    val tomorrow = calculatePrayerTimesWithContext(context, tomorrowDate, city)
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

private data class ZikrChoice(
    val value: String,
    val tm: String,
    val ru: String,
    val en: String,
    val tr: String
)

private fun ZikrChoice.label(language: AppLanguage): String =
    localized(language, tm, ru, en, tr)

private val PopularZikrs = listOf(
    ZikrChoice("Subhanallah", "Subhanallah — Alla ähli kemçiliklerden päkdir", "Субханаллах — Пречист Аллах", "Subhanallah — Glory be to Allah", "Sübhanallah — Allah noksanlıklardan münezzehtir"),
    ZikrChoice("Alhamdulillah", "Alhamdulillah — Ähli hamd Alla mahsusdyr", "Альхамдулиллях — Хвала Аллаху", "Alhamdulillah — All praise is due to Allah", "Elhamdülillah — Hamd Allah'a mahsustur"),
    ZikrChoice("Allahu Akbar", "Allahu Akbar — Alla iň Beýikdir", "Аллаху Акбар — Аллах Велик", "Allahu Akbar — Allah is the Greatest", "Allahu Ekber — Allah en büyüktür"),
    ZikrChoice("Astaghfirullah", "Astaghfirullah — Alladan bagyşlanmagy dileýärin", "Астагфируллах — Прошу у Аллаха прощения", "Astaghfirullah — I seek Allah's forgiveness", "Estağfirullah — Allah'tan bağışlanma dilerim"),
    ZikrChoice("La ilaha illallah", "La ilaha illallah — Alladan başga ilah ýokdur", "Ля иляха илляллах — Нет божества, кроме Аллаха", "La ilaha illallah — There is no deity but Allah", "Lâ ilâhe illallah — Allah'tan başka ilah yoktur"),
    ZikrChoice("Subhanallahi wa bihamdihi", "Subhanallahi wa bihamdihi — Tesbih we hamd", "Субханаллахи ва бихамдихи — Прославление и хвала", "Subhanallahi wa bihamdihi — Glory and praise", "Sübhanallahi ve bihamdihi — Tesbih ve hamd"),
    ZikrChoice("Subhanallahil azim", "Subhanallahil azim — Beýik Allany tesbih etmek", "Субханаллахиль-Азым — Прославление Великого Аллаха", "Subhanallahil azim — Glory be to Allah the Magnificent", "Sübhanallahil Azîm — Yüce Allah'ı tesbih"),
    ZikrChoice("La hawla wa la quwwata illa billah", "La hawla wa la quwwata illa billah — Güýç-kuwwat diňe Alla bilendir", "Ля хауля ва ля куввата илля биллях — Сила только от Аллаха", "La hawla wa la quwwata illa billah — There is no power except through Allah", "Lâ havle ve lâ kuvvete illâ billâh — Güç yalnız Allah'tandır"),
    ZikrChoice("Hasbunallahu wa ni'mal wakil", "Hasbunallahu wa ni'mal wakil — Alla bize ýeterlikdir", "Хасбуналлаху ва ни'маль вакиль — Нам достаточно Аллаха", "Hasbunallahu wa ni'mal wakil — Allah is sufficient for us", "Hasbunallahu ve ni'mel vekîl — Allah bize yeter"),
    ZikrChoice("Allahumma salli ala Muhammad", "Allahumma salli ala Muhammad — Salawat", "Аллахумма салли аля Мухаммад — Салават", "Allahumma salli ala Muhammad — Salawat", "Allahümme salli alâ Muhammed — Salavat"),
    ZikrChoice("La ilaha illallah wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa huwa 'ala kulli shay'in qadir", "Töwhid — La ilaha illallah wahdahu...", "Таухид — Ля иляха илляллаху вахдаху...", "Tawhid — La ilaha illallah wahdahu...", "Tevhid — Lâ ilâhe illallahu vahdehu..."),
    ZikrChoice("Subhanallahi wa bihamdihi, 'adada khalqihi, wa rida nafsihi, wa zinata 'arshihi, wa midada kalimatihi", "Ertirki tesbih — Subhanallahi wa bihamdihi...", "Утренний зикр — Субханаллахи ва бихамдихи...", "Morning dhikr — Subhanallahi wa bihamdihi...", "Sabah zikri — Sübhanallahi ve bihamdihi..."),
    ZikrChoice("Allahumma Antas-Salamu wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram", "Namazdan soňky doga — Allahumma Antas-Salam", "После намаза — Аллахумма Антас-Салям", "After prayer — Allahumma Antas-Salam", "Namazdan sonra — Allahümme Entes-Selâm"),
    ZikrChoice("Rabbi ishrah li sadri, wa yassir li amri, wahlul 'uqdatan min lisani, yafqahu qawli", "Musa pygamberiň işi ýeňilleşdirmek dogasy", "Дуа Мусы об облегчении дела", "Prayer of Musa for ease", "Musa Peygamberin kolaylık duası"),
    ZikrChoice("Rabbi zidni 'ilma", "Ylym üçin doga — Rabbi zidni ilma", "Дуа о знании — Рабби зидни ильма", "Prayer for knowledge — Rabbi zidni ilma", "İlim duası — Rabbi zidni ilmen"),
    ZikrChoice("La ilaha illa Anta subhanaka inni kuntu minaz-zalimin", "Ýunus pygamberiň dogasy", "Дуа пророка Юнуса", "Prayer of Prophet Yunus", "Yunus Peygamberin duası"),
    ZikrChoice("Anni massaniyad-durru wa Anta arhamur-rahimin", "Aýýub pygamberiň hassalyk dogasy", "Дуа пророка Айюба при болезни", "Prayer of Prophet Ayyub during illness", "Eyyub Peygamberin hastalık duası"),
    ZikrChoice("Allahumma Rabb an-nas, adhhib al-ba's, ishfi Antash-Shafi, la shifa'a illa shifa'uk, shifa'an la yughadiru saqama", "Şypa dogasy", "Дуа об исцелении", "Prayer for healing", "Şifa duası"),
    ZikrChoice("Rabbana zalamna anfusana wa in lam taghfir lana wa tarhamna lanakunanna minal-khasirin", "Toba dogasy", "Дуа покаяния", "Prayer of repentance", "Tövbe duası"),
    ZikrChoice("Rabbighfir li wa li-akhi wa adkhilna fi rahmatika wa Anta arhamur-rahimin", "Musa pygamberiň özi we dogany üçin dogasy", "Дуа Мусы за себя и брата", "Musa's prayer for himself and his brother", "Musa'nın kendisi ve kardeşi için duası"),
    ZikrChoice("Ya Fattah", "Ýa Fattah — Açýan", "Я Фаттах — Открывающий", "Ya Fattah — The Opener", "Ya Fettah — Açan"),
    ZikrChoice("Ya Razzaq", "Ýa Razzaq — Rysgal berýän", "Я Раззак — Дарующий удел", "Ya Razzaq — The Provider", "Ya Rezzak — Rızık veren"),
    ZikrChoice("Ya Ghaniyy", "Ýa Ganiý — Baý, hiç zada mätäç däl", "Я Ганий — Богатый, ни в чём не нуждающийся", "Ya Ghaniyy — The Self-Sufficient", "Ya Ganiyy — Hiçbir şeye muhtaç olmayan"),
    ZikrChoice("Ya Mughni", "Ýa Mugni — Baý edýän", "Я Мугни — Обогащающий", "Ya Mughni — The Enricher", "Ya Muğni — Zengin eden")
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
    val savedLocationMode = remember { preferences.getString("location_mode", "gps") ?: "gps" }

    var selectedCity by remember {
        mutableStateOf(Cities.firstOrNull { it.name == savedCityName } ?: Cities.first { it.name == "Köneürgenç" })
    }
    var language by remember {
        mutableStateOf(AppLanguage.entries.firstOrNull { it.code == savedLanguageCode } ?: AppLanguage.TM)
    }
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var now by remember { mutableStateOf(ZonedDateTime.now(TurkmenistanZone)) }
    var locationMode by remember { mutableStateOf(savedLocationMode) }
    var prayerSettingsRevision by remember { mutableIntStateOf(0) }

    fun saveCity(city: City, mode: String) {
        selectedCity = city
        locationMode = mode
        preferences.edit()
            .putString("city", city.name)
            .putString("location_mode", mode)
            .apply()
    }

    fun chooseManualCity(city: City) {
        saveCity(city, "manual")
    }

    fun chooseGpsCity(city: City) {
        if (locationMode == "gps") {
            saveCity(city, "gps")
        }
    }

    fun prayerSettingsChanged() {
        prayerSettingsRevision++
    }

    fun chooseLanguage(newLanguage: AppLanguage) {
        language = newLanguage
        preferences.edit().putString("language", newLanguage.code).apply()
    }

    fun refreshLocation() {
        locationMode = "gps"
        preferences.edit().putString("location_mode", "gps").apply()
        detectNearestCity(context) { detectedCity -> chooseGpsCity(detectedCity) }
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
    if (locationMode == "gps") {
        requestLocation()
    }
}

    val text = uiText(language)
    val prayerNames = prayerLabels(language)
    val prayerTimes = remember(selectedCity, now.toLocalDate(), prayerSettingsRevision) {
        calculatePrayerTimesWithContext(context, now.toLocalDate(), selectedCity)
    }
    val nextPrayer = findNextPrayer(context, now, selectedCity, prayerTimes, prayerNames)
    val countdown = countdownText(now, nextPrayer)

    LaunchedEffect(selectedCity.name, language.code, now.toLocalDate(), prayerSettingsRevision) {
        reschedulePrayerEvents(
            context = context,
            city = selectedCity,
            labels = prayerNames,
            languageCode = language.code
        )
    }

    LaunchedEffect("clock") {
        while (true) {
            now = ZonedDateTime.now(TurkmenistanZone)
            delay(1000)
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
                    onCitySelected = ::chooseManualCity,
                    onNotifications = { selectedTab = AppTab.PRAYER },
                    onDhikr = { selectedTab = AppTab.DHIKR },
                    onTasbih = { selectedTab = AppTab.TASBIH }
                )
                AppTab.PRAYER -> NotificationSettingsScreen(
                    city = selectedCity,
                    labels = prayerNames,
                    language = language,
                    settingsRevision = prayerSettingsRevision,
                    onPrayerSettingsChanged = ::prayerSettingsChanged
                )
                AppTab.DHIKR -> DhikrScreen(text, language)
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
    onCitySelected: (City) -> Unit,
    onNotifications: () -> Unit,
    onDhikr: () -> Unit,
    onTasbih: () -> Unit
) {
    var languageMenuOpen by remember { mutableStateOf(false) }
    var cityChooserOpen by remember { mutableStateOf(false) }
    val labels = prayerLabels(language)

    if (cityChooserOpen) {
        AlertDialog(
            onDismissRequest = { cityChooserOpen = false },
            title = {
                Text(
                    localized(
                        language,
                        "Şäheri saýla",
                        "Выберите город",
                        "Choose city",
                        "Şehir seç"
                    )
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(Cities) { option ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onCitySelected(option)
                                cityChooserOpen = false
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (option.name == city.name) Gold else SoftGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(option.name, fontWeight = FontWeight.Bold, color = DeepGreen)
                                Text(
                                    regionLabel(option, language),
                                    color = DeepGreen.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F5F1), Ivory, Ivory)))
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text("NAMAZ WAGTY", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text(
                localized(
                    language,
                    "Zikir we dogalar • v1.2",
                    "Зикр и дуа • v1.2",
                    "Dhikr & Duas • v1.2",
                    "Zikir ve dualar • v1.2"
                ),
                fontSize = 14.sp,
                color = Green
            )
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
                                 "🕌 ${prayerCalculationModeLabel(LocalContext.current, language)}",
                                 color = Gold,
                                 fontSize = 11.sp
                             )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = onAutoLocation,
                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepGreen)
                            ) { Text("GPS") }
                            Button(
                                onClick = { cityChooserOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftGreen, contentColor = DeepGreen)
                            ) {
                                Text(
                                    localized(language, "Şäher", "Город", "City", "Şehir"),
                                    fontSize = 12.sp
                                )
                            }
                        }
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
        item { QuickAction("✦", text.dhikr, text.dhikrDuaTitle, onDhikr) }
        item { QuickAction("●", text.tasbih, text.counter, onTasbih) }
        item { QuickAction("🔔", notificationSettingsTitle(language), notificationSettingsSubtitle(language), onNotifications) }
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
private fun PrayerNotificationCard(
    language: AppLanguage,
    onSettingsChanged: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }

    var selectedMinutes by remember {
        mutableIntStateOf(preferences.getInt("reminder_minutes", 10))
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            localized(language, "🔔 Bildirişler", "🔔 Уведомления", "🔔 Notifications", "🔔 Bildirimler"),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DeepGreen
        )
        Text(
            localized(
                language,
                "Öňünden duýdurmak",
                "Напомнить заранее",
                "Remind before prayer",
                "Önceden hatırlat"
            ),
            color = Green
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(listOf(5, 10, 15, 30)) { minute ->
                Button(
                    onClick = {
                        selectedMinutes = minute
                        preferences.edit().putInt("reminder_minutes", minute).apply()
                        onSettingsChanged()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMinutes == minute) Gold else SoftGreen,
                        contentColor = DeepGreen
                    )
                ) {
                    Text(minute.toString() + " min")
                }
            }
        }
    }
}

@Composable
private fun AzanSettingsCard(
    language: AppLanguage,
    onSettingsChanged: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }

    var azan by remember { mutableStateOf(preferences.getBoolean("azan_enabled", false)) }
    var vibration by remember { mutableStateOf(preferences.getBoolean("vibration_enabled", true)) }
    var melodyEnabled by remember { mutableStateOf(preferences.getBoolean("melody_enabled", true)) }
    var selectedAzan by remember {
        mutableStateOf(
            preferences.getString("azan_sound", "Azan 1")
                ?.replace("Азан", "Azan")
                ?: "Azan 1"
        )
    }

    fun soundLabel(key: String): String = when (key) {
        "Melody Oasis" -> localized(
            language,
            "Oazis — 15 sek.",
            "Оазис — 15 сек.",
            "Oasis — 15 sec.",
            "Vaha — 15 sn."
        )
        "Melody Nasheed" -> localized(
            language,
            "Arap naşidi — 15 sek.",
            "Арабский нашид — 15 сек.",
            "Arabic nasheed — 15 sec.",
            "Arap neşidi — 15 sn."
        )
        "Melody Ney" -> localized(
            language,
            "Sufi neý — 15 sek.",
            "Суфийский ней — 15 сек.",
            "Sufi ney — 15 sec.",
            "Sufi ney — 15 sn."
        )
        "Arabic Melody", "Turkish Melody", "Melody 1", "Melody 2" ->
            localized(language, "Oazis — 15 sek.", "Оазис — 15 сек.", "Oasis — 15 sec.", "Vaha — 15 sn.")
        else -> key
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            localized(language, "📢 Azan sazlamalary", "📢 Настройки азана", "📢 Adhan settings", "📢 Ezan ayarları"),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DeepGreen
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    azan = !azan
                    preferences.edit().putBoolean("azan_enabled", azan).apply()
                    onSettingsChanged()
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
                    onSettingsChanged()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vibration) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text(
                    if (vibration)
                        localized(language, "Wibrasiýa ON", "Вибрация ON", "Vibration ON", "Titreşim ON")
                    else
                        localized(language, "Wibrasiýa OFF", "Вибрация OFF", "Vibration OFF", "Titreşim OFF")
                )
            }
        }

        Button(
            onClick = {
                melodyEnabled = !melodyEnabled
                preferences.edit().putBoolean("melody_enabled", melodyEnabled).apply()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (melodyEnabled) Gold else SoftGreen,
                contentColor = DeepGreen
            )
        ) {
            Text(
                if (melodyEnabled)
                    localized(language, "Melodiýa ON", "Мелодия ON", "Melody ON", "Melodi ON")
                else
                    localized(language, "Melodiýa OFF", "Мелодия OFF", "Melody OFF", "Melodi OFF")
            )
        }

        Text(
            localized(language, "Ses: ", "Звук: ", "Sound: ", "Ses: ") + soundLabel(selectedAzan),
            color = Green
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Azan 1", "Azan 2", "Melody Oasis", "Melody Nasheed", "Melody Ney")) { sound ->
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
                    Text(soundLabel(sound), fontSize = 12.sp)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { playAzan(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text(
                    localized(language, "🔊 Barla", "🔊 Проверить", "🔊 Test", "🔊 Test")
                )
            }

            Button(
                onClick = { stopAzanPlayback(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DeepGreen
                )
            ) {
                Text(
                    localized(language, "■ Duruz", "■ Стоп", "■ Stop", "■ Durdur")
                )
            }
        }

        Text(
            localized(
                language,
                "Azan ýa-da namaz üçin saz başlasa, programmadan çyksaňyz hem soňuna çenli dowam eder.",
                "Азан или мелодия уведомления о намазе продолжат играть до конца, даже если выйти или закрыть приложение.",
                "Adhan or prayer notification melody keeps playing to the end even if you leave or close the app.",
                "Ezan veya namaz bildirim melodisi, uygulamadan çıksanız bile sonuna kadar çalmaya devam eder."
            ),
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AsrCalculationSettingsCard(
    language: AppLanguage,
    onSettingsChanged: () -> Unit
) {
    val context = LocalContext.current
    var method by remember { mutableStateOf(getAsrCalculationMethod(context)) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            localized(language, "🕌 Asr hasaplama usuly", "🕌 Метод Аср", "🕌 Asr method", "🕌 İkindi yöntemi"),
            fontSize = 18.sp,
            color = DeepGreen
        )

        Text(
            localized(
                language,
                "Diňe oflaýn ätiýaçlyk hasaplamasyna täsir edýär.",
                "Влияет только на резервный офлайн-расчёт.",
                "Only affects the offline backup calculation.",
                "Yalnızca çevrimdışı yedek hesabı etkiler."
            ),
            color = Color.Gray,
            fontSize = 12.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    method = AsrCalculationMethod.HANAFI
                    saveAsrCalculationMethod(context, method)
                    onSettingsChanged()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (method == AsrCalculationMethod.HANAFI) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text(localized(language, "Hanafi", "Ханафи", "Hanafi", "Hanefi"))
            }

            Button(
                onClick = {
                    method = AsrCalculationMethod.SHAFII
                    saveAsrCalculationMethod(context, method)
                    onSettingsChanged()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (method == AsrCalculationMethod.SHAFII) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text(localized(language, "Şafygy", "Шафи'и", "Shafi'i", "Şafii"))
            }
        }
    }
}

@Composable
private fun PrayerCalculationSettingsCard(
    language: AppLanguage,
    onSettingsChanged: () -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(getPrayerCalculationMode(context)) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            localized(
                language,
                "🕌 Namaz wagtyny hasaplama usuly",
                "🕌 Метод расчёта времени намаза",
                "🕌 Prayer time method",
                "🕌 Namaz vakti yöntemi"
            ),
            fontSize = 18.sp,
            color = DeepGreen
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    mode = PrayerCalculationMode.MUFTIATE_TKM
                    savePrayerCalculationMode(context, mode)
                    onSettingsChanged()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == PrayerCalculationMode.MUFTIATE_TKM) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text(localized(language, "Müftülik TKM", "Муфтият ТКМ", "Muftiate TKM", "Müftülük TKM"))
            }

            Button(
                onClick = {
                    mode = PrayerCalculationMode.OFFLINE_BACKUP
                    savePrayerCalculationMode(context, mode)
                    onSettingsChanged()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == PrayerCalculationMode.OFFLINE_BACKUP) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text(localized(language, "Oflaýn", "Офлайн", "Offline", "Çevrimdışı"))
            }
        }
    }
}

@Composable
private fun PrayerParametersCard(language: AppLanguage, settingsRevision: Int) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            localized(
                language,
                "📐 Hasaplama parametrleri",
                "📐 Параметры расчёта",
                "📐 Calculation parameters",
                "📐 Hesaplama parametreleri"
            ),
            fontSize = 18.sp,
            color = DeepGreen
        )

        Text(
            prayerParametersLabel(context, language),
            color = Green,
            fontSize = 14.sp
        )

        Text(
            when (getPrayerCalculationMode(context)) {
                PrayerCalculationMode.MUFTIATE_TKM -> localized(
                    language,
                    "Asyl Namaz wagty maglumat bazasyndaky sebit tertibi ulanylýar.",
                    "Используется региональное расписание из исходной базы Namaz wagty.",
                    "The regional timetable from the original Namaz wagty database is used.",
                    "Orijinal Namaz wagty veritabanındaki bölgesel takvim kullanılıyor."
                )
                PrayerCalculationMode.OFFLINE_BACKUP -> localized(
                    language,
                    "Bu režimde wagtlar astronomiki formula bilen ätiýaçlyk hökmünde hasaplanýar.",
                    "В этом режиме время рассчитывается резервной астрономической формулой.",
                    "In this mode prayer times use the backup astronomical calculation.",
                    "Bu modda vakitler yedek astronomik hesaplamayla belirlenir."
                )
            },
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun NotificationSettingsScreen(
    city: City,
    labels: PrayerLabels,
    language: AppLanguage,
    settingsRevision: Int,
    onPrayerSettingsChanged: () -> Unit
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
                    val refreshSchedules = {
                        onPrayerSettingsChanged()
                    }

                    PrayerNotificationCard(language, refreshSchedules)
                    AzanSettingsCard(language, refreshSchedules)
                    PrayerCalculationSettingsCard(language, refreshSchedules)
                    PrayerParametersCard(language, settingsRevision)
                    AsrCalculationSettingsCard(language, refreshSchedules)

                    var prayerTimeNotification by remember {
                        mutableStateOf(preferences.getBoolean("prayer_time_notification", false))
                    }

                    Text(
                        localized(
                            language,
                            "🕌 Namaz wagtyndaky bildiriş",
                            "🕌 Уведомление при наступлении времени намаза",
                            "🕌 Notification at prayer time",
                            "🕌 Namaz vaktinde bildirim"
                        ),
                        color = DeepGreen
                    )

                    val prayerNamesList = listOf(
                        labels.fajr,
                        labels.dhuhr,
                        labels.asr,
                        labels.maghrib,
                        labels.isha
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
                                refreshSchedules()
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
                            refreshSchedules()
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
private fun DhikrScreen(text: UiText, language: AppLanguage) {
    var selected by remember { mutableStateOf<String?>(null) }
    var expandedItems by remember { mutableStateOf(setOf<Int>()) }
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }

    fun t(tm: String, ru: String, en: String, tr: String): String =
        localized(language, tm, ru, en, tr)

    val ayatKursi = "Allahu la ilaha illa Huwa, Al-Hayyul-Qayyum. La ta'khudhuhu sinatun wa la nawm. Lahu ma fis-samawati wa ma fil-ard. Man dhal-ladhi yashfa'u 'indahu illa bi-idhnih. Ya'lamu ma bayna aydihim wa ma khalfahum. Wa la yuhituna bi-shay'in min 'ilmihi illa bima sha'. Wasi'a kursiyyuhus-samawati wal-ard. Wa la ya'uduhu hifzuhuma. Wa Huwal-'Aliyyul-'Azim."
    val tahleel = "La ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa huwa 'ala kulli shay'in qadir."
    val categories = linkedMapOf(
        t("☀ Ertir namazyndan soň", "☀ После Фаджра", "☀ After Fajr", "☀ Sabah namazından sonra") to listOf(
            DhikrItem(
                t("Aýat al-Kursi ×1", "Аят аль-Курси ×1", "Ayat al-Kursi ×1", "Ayetel Kürsi ×1"),
                "",
                ayatKursi,
                t(
                    "Alla — Ondan başga ybadat edilmäge hakly ilah ýokdur. Ol Diridir we ähli zady dolandyrýandyr. Ony ne ukusyrama, ne-de uky tutar. Asmanlardaky we ýerdäki ähli zat Onuňkydyr. Onuň rugsady bolmasa hiç kim Onuň huzurynda şepagat edip bilmez. Ol olaryň öňündäki we arkasyndaky zatlary bilýär. Olar bolsa Onuň ylmyndan diňe Onuň islänini gurşap bilerler. Onuň Kursisi asmanlary we ýeri gurşap alandyr. Olary goramak Oňa kyn düşmez. Ol Beýikdir, Uludyr.",
                    "Аллах — нет божества, достойного поклонения, кроме Него, Живого, Вседержителя. Им не овладевают ни дремота, ни сон. Ему принадлежит всё на небесах и на земле. Кто станет заступаться перед Ним без Его дозволения? Он знает их будущее и прошлое. Они постигают из Его знания лишь то, что Он пожелает. Его Курси объемлет небеса и землю, и не тяготит Его охрана их. Он — Возвышенный, Великий.",
                    "Allah—there is no deity worthy of worship except Him, the Ever-Living, the Sustainer. Neither drowsiness nor sleep overtakes Him. To Him belongs whatever is in the heavens and the earth. No one can intercede except by His permission. He knows what is before and behind them, and they grasp only what He wills of His knowledge. His Kursi extends over the heavens and the earth, and preserving them does not tire Him. He is the Most High, the Great.",
                    "Allah, O'ndan başka ibadete layık ilah yoktur; O diridir ve her şeyi ayakta tutandır. O'nu ne uyuklama ne de uyku tutar. Göklerde ve yerde ne varsa O'nundur. İzni olmadan O'nun katında kim şefaat edebilir? O, önlerindekini ve arkalarındakini bilir. Onlar O'nun ilminden ancak dilediği kadarını kavrayabilir. Kürsüsü gökleri ve yeri kuşatmıştır; onları korumak O'na ağır gelmez. O yücedir, büyüktür."
                ),
                t("Ertir namazyndan soň", "После Фаджра", "After Fajr", "Sabah namazından sonra"),
                t("Alla ýatlamak we gorag dilemek.", "Поминание Аллаха и просьба о защите.", "Remembering Allah and seeking protection.", "Allah'ı anmak ve korunma dilemek."),
                t("Gurhan 2:255", "Коран 2:255", "Quran 2:255", "Kur'an 2:255"),
                1
            ),
            DhikrItem(
                t("Ertirki tesbih ×3", "Утренний зикр ×3", "Morning dhikr ×3", "Sabah zikri ×3"),
                "",
                "Subhanallahi wa bihamdihi, 'adada khalqihi, wa rida nafsihi, wa zinata 'arshihi, wa midada kalimatihi.",
                t(
                    "Allany mahluklarynyň sanyça, Öz razylygyça, Arşynyň agramyça we sözleriniň syýasyça päk diýip tesbih edýärin hem-de Oňa hamd aýdýaryn.",
                    "Пречист Аллах и хвала Ему — по числу Его творений, по мере Его довольства, по весу Его Трона и по количеству чернил для Его слов.",
                    "Glory and praise be to Allah—as many as His creation, as much as pleases Him, as heavy as His Throne, and as vast as the ink for His words.",
                    "Allah'ı yaratılmışlarının sayısınca, razı olacağı kadar, Arş'ının ağırlığınca ve kelimelerinin mürekkebi kadar tesbih eder ve O'na hamd ederim."
                ),
                t("Irden", "Утром", "In the morning", "Sabah"),
                t("Tesbih we hamd.", "Прославление и хвала Аллаху.", "Glorification and praise of Allah.", "Tesbih ve hamd."),
                t("Sahih Muslim", "Сахих Муслим", "Sahih Muslim", "Sahih Müslim"),
                3
            ),
            DhikrItem(
                t("Töwhid ×100", "Таухид ×100", "Tawhid ×100", "Tevhid ×100"),
                "",
                tahleel,
                t(
                    "Alladan başga ybadat edilmäge hakly ilah ýokdur. Ol ýeke-täkdir, şärigi ýokdur. Mülk hem, hamd hem Onuňkydyr. Ol ähli zada Kadyrdyr.",
                    "Нет божества, достойного поклонения, кроме одного Аллаха, у Которого нет сотоварища. Ему принадлежит власть и хвала, и Он способен на всякую вещь.",
                    "There is no deity worthy of worship except Allah alone, without partner. To Him belong sovereignty and praise, and He has power over all things.",
                    "Allah'tan başka ibadete layık ilah yoktur; O tektir, ortağı yoktur. Mülk ve hamd O'nundur ve O her şeye kadirdir."
                ),
                t("Irden ýa-da günüň dowamynda", "Утром или в течение дня", "Morning or during the day", "Sabah veya gün içinde"),
                t("Töwhid we zikr.", "Таухид и поминание Аллаха.", "Tawhid and remembrance of Allah.", "Tevhid ve zikir."),
                t("Sahih hadyslar", "Достоверные хадисы", "Authentic hadiths", "Sahih hadisler"),
                100
            )
        ),
        t("💼 Işe başlamazdan öň", "💼 Перед работой", "💼 Before work", "💼 İşten önce") to listOf(
            DhikrItem(
                t("Musa pygamberiň (a.s.) işi ýeňilleşdirmek dogasy ×1", "Дуа пророка Мусы (а.с.) об облегчении дела ×1", "Prophet Musa's prayer for ease ×1", "Musa Peygamberin işi kolaylaştırma duası ×1"),
                "",
                "Rabbi ishrah li sadri, wa yassir li amri, wahlul 'uqdatan min lisani, yafqahu qawli.",
                t(
                    "Eý, Rebbim! Döşümi giňelt, işimi ýeňilleşdir, dilimdäki düwüni çöz, sözlerime düşünsünler.",
                    "Господи! Раскрой мою грудь, облегчи моё дело и развяжи узел на моём языке, чтобы они понимали мою речь.",
                    "My Lord, expand my chest, ease my task, and untie the knot from my tongue so that they may understand my speech.",
                    "Rabbim! Göğsümü genişlet, işimi kolaylaştır ve dilimdeki düğümü çöz ki sözümü anlasınlar."
                ),
                t("Işiň, gepleşigiň ýa-da synagyň öň ýanynda", "Перед работой, разговором или экзаменом", "Before work, a conversation, or an exam", "İş, görüşme veya sınav öncesi"),
                t("Işiň ýeňilleşmegi we sözleriň düşnükli bolmagy.", "Облегчение дела и ясность речи.", "Ease in the task and clarity of speech.", "İşin kolaylaşması ve sözün anlaşılması."),
                t("Gurhan 20:25–28", "Коран 20:25–28", "Quran 20:25–28", "Kur'an 20:25–28"),
                1
            ),
            DhikrItem(
                t("Ylym üçin doga ×1", "Дуа о знании ×1", "Prayer for knowledge ×1", "İlim duası ×1"),
                "",
                "Rabbi zidni 'ilma.",
                t("Eý, Rebbim! Ylmymy artdyr.", "Господи! Приумножь мои знания.", "My Lord, increase me in knowledge.", "Rabbim! İlmimi artır."),
                t("Okuwdan öň", "Перед учёбой", "Before studying", "Ders çalışmadan önce"),
                t("Peýdaly ylym dilemek.", "Просьба о полезном знании.", "Seeking beneficial knowledge.", "Faydalı ilim istemek."),
                t("Gurhan 20:114", "Коран 20:114", "Quran 20:114", "Kur'an 20:114"),
                1
            )
        ),
        t("🕌 Namazdan soň", "🕌 После намаза", "🕌 After prayer", "🕌 Namazdan sonra") to listOf(
            DhikrItem(
                t("Istigfar ×3", "Истигфар ×3", "Istighfar ×3", "İstiğfar ×3"),
                "",
                "Astaghfirullah.",
                t("Alladan bagyşlanmagy dileýärin.", "Прошу у Аллаха прощения.", "I seek Allah's forgiveness.", "Allah'tan bağışlanma dilerim."),
                t("Farz namazdan soň", "После обязательного намаза", "After an obligatory prayer", "Farz namazdan sonra"),
                t("Bagyşlanmak dilemek.", "Просьба о прощении.", "Seeking forgiveness.", "Bağışlanma dilemek."),
                t("Sahih Muslim", "Сахих Муслим", "Sahih Muslim", "Sahih Müslim"),
                3
            ),
            DhikrItem(
                t("Allahumma Antas-Salam ×1", "Аллахумма Антас-Салям ×1", "Allahumma Antas-Salam ×1", "Allahumma Antas-Salam ×1"),
                "",
                "Allahumma Antas-Salamu wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram.",
                t(
                    "Eý Allah! Sen As-Salamsyň, salamatlyk Senden gelýär. Eý, beýiklik we kerem eýesi, Sen bereketlidirsiň.",
                    "О Аллах! Ты — Ас-Салям, и от Тебя мир. Благословен Ты, Обладатель величия и почёта.",
                    "O Allah, You are Peace and from You comes peace. Blessed are You, Possessor of Majesty and Honor.",
                    "Allah'ım! Sen es-Selâm'sın, selamet Sendendir. Ey celâl ve ikram sahibi, Sen yücesin ve bereketlisin."
                ),
                t("Farz namazdan soň", "После обязательного намаза", "After an obligatory prayer", "Farz namazdan sonra"),
                t("Allany zikr etmek.", "Поминание Аллаха.", "Remembering Allah.", "Allah'ı zikretmek."),
                t("Sahih Muslim", "Сахих Муслим", "Sahih Muslim", "Sahih Müslim"),
                1
            ),
            DhikrItem(
                t("Aýat al-Kursi ×1", "Аят аль-Курси ×1", "Ayat al-Kursi ×1", "Ayetel Kürsi ×1"),
                "",
                ayatKursi,
                t("Aýat al-Kursiniň doly transkripsiýasy.", "Полная транскрипция Аята аль-Курси.", "Full transliteration of Ayat al-Kursi.", "Ayetel Kürsi'nin tam okunuşu."),
                t("Farz namazdan soň", "После обязательного намаза", "After an obligatory prayer", "Farz namazdan sonra"),
                t("Zikr we gorag dilemek.", "Поминание и просьба о защите.", "Remembrance and protection.", "Zikir ve korunma."),
                t("Gurhan 2:255", "Коран 2:255", "Quran 2:255", "Kur'an 2:255"),
                1
            ),
            DhikrItem(
                t("Subhanallah ×33", "Субханаллах ×33", "Subhanallah ×33", "Subhanallah ×33"),
                "",
                "Subhanallah.",
                t("Allah ähli kemçiliklerden päkdir.", "Пречист Аллах.", "Glory be to Allah.", "Allah noksanlıklardan münezzehtir."),
                t("Namazdan soň", "После намаза", "After prayer", "Namazdan sonra"),
                t("Tesbih.", "Прославление Аллаха.", "Glorification.", "Tesbih."),
                t("Sahih hadyslar", "Достоверные хадисы", "Authentic hadiths", "Sahih hadisler"),
                33
            ),
            DhikrItem(
                t("Alhamdulillah ×33", "Альхамдулиллях ×33", "Alhamdulillah ×33", "Elhamdülillah ×33"),
                "",
                "Alhamdulillah.",
                t("Ähli hamd Alla mahsusdyr.", "Хвала Аллаху.", "All praise is due to Allah.", "Hamd Allah'a mahsustur."),
                t("Namazdan soň", "После намаза", "After prayer", "Namazdan sonra"),
                t("Şükür we hamd.", "Благодарность и хвала.", "Gratitude and praise.", "Şükür ve hamd."),
                t("Sahih hadyslar", "Достоверные хадисы", "Authentic hadiths", "Sahih hadisler"),
                33
            ),
            DhikrItem(
                t("Allahu Akbar ×33", "Аллаху Акбар ×33", "Allahu Akbar ×33", "Allahu Ekber ×33"),
                "",
                "Allahu Akbar.",
                t("Allah iň Beýikdir.", "Аллах Велик.", "Allah is the Greatest.", "Allah en büyüktür."),
                t("Namazdan soň", "После намаза", "After prayer", "Namazdan sonra"),
                t("Allany beýgeltmek.", "Возвеличивание Аллаха.", "Magnifying Allah.", "Allah'ı yüceltmek."),
                t("Sahih hadyslar", "Достоверные хадисы", "Authentic hadiths", "Sahih hadisler"),
                33
            ),
            DhikrItem(
                t("Töwhid bilen tamamlamak ×1", "Завершение таухидом ×1", "Finish with tawhid ×1", "Tevhid ile tamamlama ×1"),
                "",
                tahleel,
                t(
                    "Alladan başga ybadat edilmäge hakly ilah ýokdur. Ol ýeke-täkdir, şärigi ýokdur. Mülk hem, hamd hem Onuňkydyr. Ol ähli zada Kadyrdyr.",
                    "Нет божества, достойного поклонения, кроме одного Аллаха, у Которого нет сотоварища. Ему принадлежит власть и хвала, и Он способен на всякую вещь.",
                    "There is no deity worthy of worship except Allah alone, without partner. To Him belong sovereignty and praise, and He has power over all things.",
                    "Allah'tan başka ibadete layık ilah yoktur; O tektir, ortağı yoktur. Mülk ve hamd O'nundur ve O her şeye kadirdir."
                ),
                t("Namazdan soň", "После намаза", "After prayer", "Namazdan sonra"),
                t("Zikri tamamlamak.", "Завершение зикра.", "Completing the dhikr.", "Zikri tamamlama."),
                t("Sahih Muslim", "Сахих Муслим", "Sahih Muslim", "Sahih Müslim"),
                1
            )
        ),
        t("⚠ Kynçylyk wagty", "⚠ При трудностях", "⚠ In difficulty", "⚠ Zorluk anında") to listOf(
            DhikrItem(
                t("Ýunus pygamberiň (a.s.) dogasy ×1", "Дуа пророка Юнуса (а.с.) ×1", "Prayer of Prophet Yunus ×1", "Yunus Peygamberin duası ×1"),
                "",
                "La ilaha illa Anta subhanaka inni kuntu minaz-zalimin.",
                t(
                    "Senden başga ybadat edilmäge hakly ilah ýokdur. Sen päksiň. Hakykatdan hem men zalymlyk edenlerden boldum.",
                    "Нет божества, достойного поклонения, кроме Тебя. Пречист Ты! Поистине, я был из числа несправедливых.",
                    "There is no deity worthy of worship except You. Glory be to You; indeed, I was among the wrongdoers.",
                    "Senden başka ibadete layık ilah yoktur. Seni tenzih ederim. Gerçekten ben zalimlerden oldum."
                ),
                t("Kynçylykda, gam-gussa ýa-da aladada", "При беде, тревоге или трудной ситуации", "In hardship, distress, or anxiety", "Sıkıntı, kaygı veya zorlukta"),
                t("Alladan çykalga dilemek.", "Просьба к Аллаху об избавлении.", "Seeking relief from Allah.", "Allah'tan çıkış ve ferahlık istemek."),
                t("Gurhan 21:87", "Коран 21:87", "Quran 21:87", "Kur'an 21:87"),
                1
            )
        ),
        t("🌙 Ýatmazdan öň", "🌙 Перед сном", "🌙 Before sleep", "🌙 Uyumadan önce") to listOf(
            DhikrItem(
                t("Aýat al-Kursi ×1", "Аят аль-Курси ×1", "Ayat al-Kursi ×1", "Ayetel Kürsi ×1"),
                "",
                ayatKursi,
                t("Aýat al-Kursiniň doly transkripsiýasy.", "Полная транскрипция Аята аль-Курси.", "Full transliteration of Ayat al-Kursi.", "Ayetel Kürsi'nin tam okunuşu."),
                t("Ýatmazdan öň", "Перед сном", "Before sleep", "Uyumadan önce"),
                t("Gije gorag dilemek.", "Просьба о защите на ночь.", "Seeking protection for the night.", "Gece için korunma dilemek."),
                t("Gurhan 2:255; Sahih al-Buhari", "Коран 2:255; Сахих аль-Бухари", "Quran 2:255; Sahih al-Bukhari", "Kur'an 2:255; Sahih Buhari"),
                1
            )
        ),
        t("❤️ Saglyk üçin", "❤️ За здоровье", "❤️ For health", "❤️ Sağlık için") to listOf(
            DhikrItem(
                t("Aýýub pygamberiň (a.s.) hassalyk dogasy ×1", "Дуа пророка Айюба (а.с.) при болезни ×1", "Prayer of Prophet Ayyub during illness ×1", "Eyyub Peygamberin hastalık duası ×1"),
                "",
                "Anni massaniyad-durru wa Anta arhamur-rahimin.",
                t(
                    "Maňa kynçylyk degdi, Sen bolsa rehimlileriň iň Rehimlisiň.",
                    "Меня коснулась беда, а Ты — Милостивейший из милостивых.",
                    "Adversity has touched me, and You are the Most Merciful of the merciful.",
                    "Bana sıkıntı dokundu; Sen merhametlilerin en merhametlisisin."
                ),
                t("Hassalykda, gowşaklykda ýa-da agyryda", "При болезни, слабости или боли", "During illness, weakness, or pain", "Hastalık, halsizlik veya ağrıda"),
                t("Ýeňillik we şypa dilemek.", "Просьба об облегчении и исцелении.", "Seeking relief and healing.", "Kolaylık ve şifa istemek."),
                t("Gurhan 21:83", "Коран 21:83", "Quran 21:83", "Kur'an 21:83"),
                1
            ),
            DhikrItem(
                t("Şypa dogasy ×1", "Дуа об исцелении ×1", "Prayer for healing ×1", "Şifa duası ×1"),
                "",
                "Allahumma Rabb an-nas, adhhib al-ba's, ishfi Antash-Shafi, la shifa'a illa shifa'uk, shifa'an la yughadiru saqama.",
                t(
                    "Eý Allah, adamlaryň Rebbi! Keseli aýyr, şypa ber. Şypa berýän Sensiň. Seniň şypaňdan başga şypa ýokdur. Hiç bir kesel galdyrmaýan şypa ber.",
                    "О Аллах, Господь людей! Удали болезнь и исцели. Ты — Исцеляющий. Нет исцеления, кроме Твоего исцеления; даруй исцеление, не оставляющее болезни.",
                    "O Allah, Lord of mankind, remove the harm and heal. You are the Healer. There is no healing except Your healing; grant a healing that leaves no illness.",
                    "Allah'ım, insanların Rabbi! Hastalığı gider ve şifa ver. Şifa veren Sensin. Senin şifandan başka şifa yoktur; hiçbir hastalık bırakmayan bir şifa ver."
                ),
                t("Özüň ýa-da başga biri hassalanda", "При болезни — для себя или другого человека", "For yourself or another person during illness", "Kendin veya başkası hastayken"),
                t("Şypa dilemek.", "Просьба об исцелении.", "Seeking healing.", "Şifa istemek."),
                t("Sahih al-Buhari we Muslim", "Сахих аль-Бухари и Муслим", "Sahih al-Bukhari and Muslim", "Sahih Buhari ve Müslim"),
                1
            )
        ),
        t("🌙 Agşam", "🌙 Вечером", "🌙 Evening", "🌙 Akşam") to listOf(
            DhikrItem(
                t("Toba dogasy ×1", "Дуа покаяния ×1", "Prayer of repentance ×1", "Tövbe duası ×1"),
                "",
                "Rabbana zalamna anfusana wa in lam taghfir lana wa tarhamna lanakunanna minal-khasirin.",
                t(
                    "Eý, Rebbimiz! Biz özümize zulum etdik. Eger bizi bagyşlamasaň we bize rehim etmeseň, hökman zyýan çekenlerden bolarys.",
                    "Господь наш! Мы поступили несправедливо по отношению к самим себе. Если Ты не простишь нас и не помилуешь, мы непременно окажемся среди потерпевших убыток.",
                    "Our Lord, we have wronged ourselves. If You do not forgive us and have mercy on us, we will surely be among the losers.",
                    "Rabbimiz! Biz kendimize zulmettik. Eğer bizi bağışlamaz ve bize merhamet etmezsen mutlaka kaybedenlerden oluruz."
                ),
                t("Agşam ýa-da ýalňyşlykdan soň", "Вечером или после ошибки", "In the evening or after a mistake", "Akşam veya bir hatadan sonra"),
                t("Toba we magfiret.", "Покаяние и прощение.", "Repentance and forgiveness.", "Tövbe ve bağışlanma."),
                t("Gurhan 7:23", "Коран 7:23", "Quran 7:23", "Kur'an 7:23"),
                1
            ),
            DhikrItem(
                t("Musa pygamberiň özi we dogany üçin dogasy ×1", "Дуа Мусы за себя и брата ×1", "Musa's prayer for himself and his brother ×1", "Musa'nın kendisi ve kardeşi için duası ×1"),
                "",
                "Rabbighfir li wa li-akhi wa adkhilna fi rahmatika wa Anta arhamur-rahimin.",
                t(
                    "Eý, Rebbim! Meni we doganymy bagyşla, bizi Öz rahmetiňe giriz. Sen rehimlileriň iň Rehimlisiň.",
                    "Господи! Прости меня и моего брата и введи нас в Свою милость. Ты — Милостивейший из милостивых.",
                    "My Lord, forgive me and my brother and admit us into Your mercy. You are the Most Merciful of the merciful.",
                    "Rabbim! Beni ve kardeşimi bağışla, bizi rahmetine dahil et. Sen merhametlilerin en merhametlisisin."
                ),
                t("Agşam ýa-da ýakynlar üçin doga edilende", "Вечером или при молитве за близких", "In the evening or when praying for loved ones", "Akşam veya yakınlar için dua ederken"),
                t("Bagyşlanmak we rahmet.", "Прощение и милость.", "Forgiveness and mercy.", "Bağışlanma ve rahmet."),
                t("Gurhan 7:151", "Коран 7:151", "Quran 7:151", "Kur'an 7:151"),
                1
            )
        )
    )

    LaunchedEffect(language) {
        selected = null
        expandedItems = emptySet()
    }

    if (selected == null) {
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(10.dp))
                Text(text.dhikrDuaTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text(text.chooseSection, color = Green)
            }

            items(categories.keys.toList()) { category ->
                Card(
                    Modifier.fillMaxWidth().clickable {
                        selected = category
                        expandedItems = emptySet()
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        category,
                        Modifier.padding(18.dp),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepGreen
                    )
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    } else {
        val currentCategory = selected.orEmpty()
        val list = categories[currentCategory] ?: emptyList()
        val categoryIndex = categories.keys.indexOf(currentCategory).coerceAtLeast(0)

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    selected = null
                    expandedItems = emptySet()
                }) {
                    Text(t("← Yza", "← Назад", "← Back", "← Geri"))
                }
                Spacer(Modifier.height(8.dp))
                Text(currentCategory, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            }

            items(list.size) { index ->
                val item = list[index]
                val expanded = index in expandedItems
                val countKey = "dhikr_count_" + categoryIndex + "_" + index
                var value by remember(currentCategory, index) {
                    mutableIntStateOf(preferences.getInt(countKey, 0))
                }

                Card(
                    Modifier.fillMaxWidth().clickable {
                        expandedItems = if (expanded) {
                            expandedItems - index
                        } else {
                            expandedItems + index
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                item.title,
                                modifier = Modifier.weight(1f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreen
                            )
                            Text(if (expanded) "▲" else "▼", color = Gold)
                        }

                        if (!expanded) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                t("Açmak üçin basyň", "Нажмите, чтобы раскрыть", "Tap to expand", "Açmak için dokunun"),
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        } else {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                t(
                                    "Türkmençe okalyşy: ",
                                    "Транскрипция: ",
                                    "Transliteration: ",
                                    "Okunuş: "
                                ) + dhikrReading(item.transliteration, language),
                                color = Green,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(item.translation, color = Color.Gray)

                            if (item.whenToRead.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    t("Haçan: ", "Когда: ", "When: ", "Ne zaman: ") + item.whenToRead,
                                    color = Green
                                )
                            }
                            if (item.purpose.isNotBlank()) {
                                Text(
                                    t("Maksat: ", "Цель: ", "Purpose: ", "Amaç: ") + item.purpose,
                                    color = Green
                                )
                            }
                            if (item.source.isNotBlank()) {
                                Text(
                                    t("Çeşme: ", "Источник: ", "Source: ", "Kaynak: ") + item.source,
                                    color = Gold
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val next = value + 1
                                    if (next >= item.countTarget) {
                                        value = 0
                                        vibrateComplete(context)
                                    } else {
                                        value = next
                                        vibrateShort(context)
                                    }
                                    preferences.edit().putInt(countKey, value).apply()
                                }
                            ) {
                                Text(value.toString() + " / " + item.countTarget + "  +1")
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
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
    var clickSoundEnabled by remember {
        mutableStateOf(preferences.getBoolean("tasbih_click_enabled", true))
    }
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
                                        saveZikr(zikr.value)
                                        chooserOpen = false
                                    },
                                    colors = CardDefaults.cardColors(containerColor = SoftGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            zikr.label(language),
                                            color = DeepGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (language != AppLanguage.EN) {
                                            Text(
                                                dhikrReading(zikr.value, language),
                                                color = Green,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { saveCount(0) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Green)
            ) {
                Text(text.reset)
            }

            Button(
                onClick = {
                    clickSoundEnabled = !clickSoundEnabled
                    preferences.edit()
                        .putBoolean("tasbih_click_enabled", clickSoundEnabled)
                        .apply()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (clickSoundEnabled) Gold else SoftGreen,
                    contentColor = DeepGreen
                )
            ) {
                Text(
                    if (clickSoundEnabled)
                        localized(language, "🔊 Ses ON", "🔊 Звук ON", "🔊 Sound ON", "🔊 Ses ON")
                    else
                        localized(language, "🔇 Ses OFF", "🔇 Звук OFF", "🔇 Sound OFF", "🔇 Ses OFF")
                )
            }
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
                        val selectedChoice = PopularZikrs.firstOrNull { it.value == selectedZikr }
                        Text(
                            selectedChoice?.label(language) ?: dhikrReading(selectedZikr, language),
                            color = Gold,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        if (selectedChoice != null && language != AppLanguage.EN) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                selectedChoice.value,
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
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
                        playTasbihClick(context)
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
