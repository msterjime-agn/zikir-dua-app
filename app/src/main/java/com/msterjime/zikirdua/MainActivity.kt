package com.msterjime.zikirdua

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

internal fun calculatePrayerTimes(date: LocalDate, city: City): PrayerTimes {
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
        fajr = computeTime(162.0, fajr / 24.0)
        sunrise = computeTime(179.167, sunrise / 24.0)
        dhuhr = midDay(dhuhr / 24.0)
        asr = asrTime(1.0, asr / 24.0)
        maghrib = computeTime(0.833, maghrib / 24.0)
        isha = computeTime(17.0, isha / 24.0)
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
        offlineNote = "Сейчас используется резервный офлайн-расчёт: Фаджр 18°, Иша 17°, UTC+5. Метод Муфтията Туркменистана будет подключён как основной режим на следующем этапе.",
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
    PRAYER("☾"),
    DHIKR("✦"),
    TASBIH("●")
}

private fun tabTitle(tab: AppTab, text: UiText): String = when (tab) {
    AppTab.HOME -> text.home
    AppTab.PRAYER -> text.prayerTimes
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

    LaunchedEffect("clock") {
        while (true) {
            now = ZonedDateTime.now(TurkmenistanZone)
            delay(1000)
        }
    }

    val text = uiText(language)
    val prayerNames = prayerLabels(language)
    val prayerTimes = remember(selectedCity, now.toLocalDate()) {
        calculatePrayerTimes(now.toLocalDate(), selectedCity)
    }
    val nextPrayer = findNextPrayer(now, selectedCity, prayerTimes, prayerNames)
    val countdown = countdownText(now, nextPrayer)

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
                        label = { Text(tabTitle(tab, text), fontSize = 11.sp) }
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
                    onPrayer = { selectedTab = AppTab.PRAYER },
                    onDhikr = { selectedTab = AppTab.DHIKR },
                    onTasbih = { selectedTab = AppTab.TASBIH }
                )
                AppTab.PRAYER -> PrayerScreen(
                    city = selectedCity,
                    prayerTimes = prayerTimes,
                    nextPrayerName = nextPrayer.name,
                    text = text,
                    labels = prayerNames,
                    language = language,
                    onCitySelected = ::chooseCity
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
    onPrayer: () -> Unit,
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
        item { QuickAction("☾", text.prayerTimes, text.prayerShortcut, onPrayer) }
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
private fun PrayerScreen(
    city: City,
    prayerTimes: PrayerTimes,
    nextPrayerName: String,
    text: UiText,
    labels: PrayerLabels,
    language: AppLanguage,
    onCitySelected: (City) -> Unit
) {
    var cityMenuOpen by remember { mutableStateOf(false) }
    val prayers = listOf(
        PrayerRow(labels.fajr, prayerTimes.fajr),
        PrayerRow(labels.sunrise, prayerTimes.sunrise),
        PrayerRow(labels.dhuhr, prayerTimes.dhuhr),
        PrayerRow(labels.asr, prayerTimes.asr),
        PrayerRow(labels.maghrib, prayerTimes.maghrib),
        PrayerRow(labels.isha, prayerTimes.isha)
    )

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text(text.prayerTimes, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Box {
                Button(
                    onClick = { cityMenuOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGreen, contentColor = DeepGreen)
                ) {
                    Text("📍 ${city.name}  ▾")
                }
                DropdownMenu(expanded = cityMenuOpen, onDismissRequest = { cityMenuOpen = false }) {
                    Cities.forEach { option ->
                        DropdownMenuItem(
                            text = { Text("${option.name} • ${regionLabel(option, language)}") },
                            onClick = {
                                onCitySelected(option)
                                cityMenuOpen = false
                            }
                        )
                    }
                }
            }
        }
        item {
            PrayerReminderCard(
                city = city,
                labels = labels,
                languageCode = language.code
            )
        }
        item {
            PrayerNotificationCard()
        }
        item {
            AzanSettingsCard()
        }
        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5D9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text.offlineNote,
                    modifier = Modifier.padding(14.dp),
                    fontSize = 13.sp,
                    color = Color(0xFF6B5722)
                )
            }
        }
        items(prayers) { prayer ->
            val isNext = prayer.name == nextPrayerName
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isNext) SoftGreen else Color.White)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(17.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(prayer.name, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        if (isNext) Text(text.nextShort, fontSize = 11.sp, color = Green)
                    }
                    Text(
                        prayer.time.format(TimeFormatter),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNext) DeepGreen else Green
                    )
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}


@Composable
private fun PrayerNotificationCard() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }

    var enabled by remember {
        mutableStateOf(preferences.getBoolean("prayer_notifications", true))
    }
    var beforeMinutes by remember {
        mutableIntStateOf(preferences.getInt("notification_minutes", 15))
    }
    var exactTime by remember {
        mutableStateOf(preferences.getBoolean("notification_exact_time", true))
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGreen)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "🔔 Namaz bildirişleri",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepGreen
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Bildiriş", color = Ink)
                Button(
                    onClick = {
                        enabled = !enabled
                        preferences.edit()
                            .putBoolean("prayer_notifications", enabled)
                            .apply()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (enabled) Gold else Color.White,
                        contentColor = DeepGreen
                    )
                ) {
                    Text(if (enabled) "ON" else "OFF")
                }
            }

            Text("Предупредить заранее", color = Green, fontWeight = FontWeight.SemiBold)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf(5, 10, 15, 30)) { value ->
                    Button(
                        onClick = {
                            beforeMinutes = value
                            preferences.edit()
                                .putInt("notification_minutes", value)
                                .apply()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (beforeMinutes == value) Gold else Color.White,
                            contentColor = DeepGreen
                        )
                    ) {
                        Text("${value} min")
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Во время намаза", color = Ink)
                Button(
                    onClick = {
                        exactTime = !exactTime
                        preferences.edit()
                            .putBoolean("notification_exact_time", exactTime)
                            .apply()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (exactTime) Gold else Color.White,
                        contentColor = DeepGreen
                    )
                ) {
                    Text(if (exactTime) "ON" else "OFF")
                }
            }
        }
    }
}


@Composable
private fun AzanSettingsCard() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }

    var azanEnabled by remember {
        mutableStateOf(preferences.getBoolean("azan_enabled", false))
    }

    var vibrationEnabled by remember {
        mutableStateOf(preferences.getBoolean("azan_vibration", true))
    }

    var soundName by remember {
        mutableStateOf(preferences.getString("azan_sound", "Azan 1") ?: "Azan 1")
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGreen)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "🔊 Azan sazlamalary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepGreen
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Azan", color = Ink)
                Button(
                    onClick = {
                        azanEnabled = !azanEnabled
                        preferences.edit()
                            .putBoolean("azan_enabled", azanEnabled)
                            .apply()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (azanEnabled) Gold else Color.White,
                        contentColor = DeepGreen
                    )
                ) {
                    Text(if (azanEnabled) "ON" else "OFF")
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Вибрация", color = Ink)
                Button(
                    onClick = {
                        vibrationEnabled = !vibrationEnabled
                        preferences.edit()
                            .putBoolean("azan_vibration", vibrationEnabled)
                            .apply()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vibrationEnabled) Gold else Color.White,
                        contentColor = DeepGreen
                    )
                ) {
                    Text(if (vibrationEnabled) "ON" else "OFF")
                }
            }

            Text("Звук: $soundName", color = Green)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Azan 1", "Azan 2", "Gysga").forEach { sound ->
                    Button(
                        onClick = {
                            soundName = sound
                            preferences.edit()
                                .putString("azan_sound", sound)
                                .apply()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (soundName == sound) Gold else Color.White,
                            contentColor = DeepGreen
                        )
                    ) {
                        Text(sound)
                    }
                }
            }
        }
    }
}

@Composable
private fun DhikrScreen(text: UiText) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text(text.dhikrDuaTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text(text.chooseSection, color = Green, fontSize = 14.sp)
        }
        item { SectionCard("☀", text.morningDhikr, text.morningAfter) }
        item { SectionCard("☽", text.eveningDhikr, text.eveningAfter) }
        item { SectionCard("✦", text.afterPrayer, text.dhikrPrayers) }
        item { SectionCard("☾", text.beforeSleep, text.eveningPrayers) }
        item { SectionCard("♡", text.personalPrayer, text.savedPrayers) }
    }
}

@Composable
private fun SectionCard(symbol: String, title: String, subtitle: String) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(symbol, fontSize = 27.sp, color = Gold)
            Column(Modifier.padding(start = 15.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text(subtitle, color = Color.Gray, fontSize = 13.sp)
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
                    onClick = { saveCount(count + 1) },
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
