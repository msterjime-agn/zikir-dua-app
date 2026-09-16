package com.msterjime.zikirdua

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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

private data class City(
    val region: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

private val Cities = listOf(
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

private data class PrayerTimes(
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

private fun calculatePrayerTimes(date: LocalDate, city: City): PrayerTimes {
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

private enum class AppLanguage(val code: String, val label: String) {
    TM("tm", "🇹🇲 Türkmençe"),
    RU("ru", "🇷🇺 Русский"),
    EN("en", "🇬🇧 English"),
    TR("tr", "🇹🇷 Türkçe")
}

private data class PrayerLabels(
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

private fun prayerLabels(language: AppLanguage): PrayerLabels = when (language) {
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
        counter = "Hasaplaýjy 33 / 100",
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
        counter = "Счётчик 33 / 100",
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
        counter = "Counter 33 / 100",
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
        counter = "Sayaç 33 / 100",
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

    LaunchedEffect(Unit) {
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

    fun chooseCity(city: City) {
        selectedCity = city
        preferences.edit().putString("city", city.name).apply()
    }

    fun chooseLanguage(newLanguage: AppLanguage) {
        language = newLanguage
        preferences.edit().putString("language", newLanguage.code).apply()
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
                    nextPrayer = nextPrayer,
                    countdown = countdown,
                    text = text,
                    language = language,
                    onLanguageSelected = ::chooseLanguage,
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
                AppTab.TASBIH -> TasbihScreen(text)
            }
        }
    }
}

@Composable
private fun HomeScreen(
    city: City,
    nextPrayer: NextPrayer,
    countdown: String,
    text: UiText,
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onPrayer: () -> Unit,
    onDhikr: () -> Unit,
    onTasbih: () -> Unit
) {
    var languageMenuOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F5F1), Ivory, Ivory)))
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text("NAMAZ WAGTY", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text("Zikir & Dogalar • v1.0", fontSize = 14.sp, color = Green)
            Spacer(Modifier.height(8.dp))
            Box {
                Button(
                    onClick = { languageMenuOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGreen, contentColor = DeepGreen)
                ) {
                    Text("🌐 ${language.label}  ▾", fontSize = 12.sp)
                }
                DropdownMenu(
                    expanded = languageMenuOpen,
                    onDismissRequest = { languageMenuOpen = false }
                ) {
                    AppLanguage.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                onLanguageSelected(option)
                                languageMenuOpen = false
                            }
                        )
                    }
                }
            }
        }
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DeepGreen)
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📍 ${city.name}", color = Color.White.copy(alpha = 0.88f), fontSize = 14.sp)
                    Text(
                        regionLabel(city, language),
                        color = Color.White.copy(alpha = 0.60f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(text.nextPrayer, color = Color.White.copy(alpha = 0.78f), fontSize = 14.sp)
                    Text(nextPrayer.name, color = Gold, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text(
                        nextPrayer.time.format(TimeFormatter),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("${text.timeLeft}: $countdown", color = Color.White.copy(alpha = 0.82f), fontSize = 14.sp)
                }
            }
        }
        item { Text(text.quickAccess, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = Ink) }
        item { QuickAction("☾", text.prayerTimes, text.prayerShortcut, onPrayer) }
        item { QuickAction("☀", text.morningDhikr, text.morningAfter, onDhikr) }
        item { QuickAction("☽", text.eveningDhikr, text.eveningAfter, onDhikr) }
        item { QuickAction("●", text.tasbih, text.counter, onTasbih) }
        item { Spacer(Modifier.height(18.dp)) }
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
private fun TasbihScreen(text: UiText) {
    var count by remember { mutableIntStateOf(0) }
    var target by remember { mutableIntStateOf(33) }

    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(10.dp))
        Text(text.tasbih, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
        Text("${text.target}: $target", color = Green)
        Spacer(Modifier.height(28.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DeepGreen)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("$count", fontSize = 68.sp, fontWeight = FontWeight.Bold, color = Gold)
                Text("/ $target", color = Color.White.copy(alpha = 0.75f))
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = { count += 1 },
                    modifier = Modifier.size(112.dp),
                    shape = RoundedCornerShape(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepGreen)
                ) {
                    Text("+", fontSize = 42.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { target = 33; count = 0 }) { Text("33") }
            Button(onClick = { target = 100; count = 0 }) { Text("100") }
            Button(
                onClick = { count = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Green)
            ) {
                Text(text.reset)
            }
        }
    }
}
