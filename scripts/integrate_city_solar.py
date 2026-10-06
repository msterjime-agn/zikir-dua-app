#!/usr/bin/env python3
"""Checked migration of the existing app; safe to rerun on this build branch."""
from pathlib import Path
import re, hashlib, base64, zlib

root = Path('app/src/main/java/com/msterjime/zikirdua')
path = root / 'MainActivity.kt'
s = path.read_text(encoding='utf-8')
marker = '// CITY_SOLAR_INTEGRATION_V1'
if marker in s:
    print('City solar integration is already present')
    raise SystemExit(0)
blob = hashlib.sha1(b'blob ' + str(len(s.encode())).encode() + b'\0' + s.encode()).hexdigest()
assert blob == '51b0071c193596db0603e839758fc5dee7458b0b', f'MainActivity changed: {blob}; refusing blind replacement'
original = s

def replace_once(old, new):
    global s
    assert s.count(old) == 1, f'Expected one patch location: {old[:80]!r}, found {s.count(old)}'
    s = s.replace(old, new, 1)

replace_once('return MuftiateSchedule.prayerTimes(date, city)\n        ?: calculatePrayerTimes(date, city)',
             'return CitySolarSchedule.forCity(date, city).times')
# Ensure all public context-aware paths use the same schedule as UI and alarms.
replace_once('return calculatePrayerTimes(date, city, context)\n}',
             'return calculatePrayerTimesWithContext(context, date, city)\n}')
old_mode = '''    return when (getPrayerCalculationMode(context)) {
        PrayerCalculationMode.MUFTIATE_TKM ->
            calculateMuftiateTKMPrayerTimes(date, city)
        PrayerCalculationMode.OFFLINE_BACKUP ->
            calculatePrayerTimes(date, city, context)
    }
}'''
replace_once(old_mode, '    return calculatePrayerTimesWithContext(context, date, city)\n}')
# Keep stored mode IDs compatible, but stop claiming official city-level accuracy.
s = s.replace('"Müftülik TKM", "Муфтият ТКМ", "Muftiate TKM", "Müftülük TKM"',
              '"Gün doguşy / ýaşmagy", "Восход / закат", "Sunrise / sunset", "Güneş doğuş / batış"')
for old, new in [
 ('Takyk Müftülik tertibi • sebit boýunça tablisa', 'Sebit tablisasy + şäheriň gün doguş / ýaşma tapawudy. Öýle üýtgemeýär.'),
 ('Точное расписание Муфтията • таблица по региону', 'Региональная таблица + разница восхода/заката для города. Öýle фиксирован.'),
 ('Exact Muftiate timetable • regional table', 'Regional table + city sunrise/sunset differences. Dhuhr stays fixed.'),
 ('Kesin Müftülük takvimi • bölgesel tablo', 'Bölgesel tablo + şehrin doğuş/batış farkı. Öğle sabit kalır.'),
 ('Asyl Namaz wagty maglumat bazasyndaky sebit tertibi ulanylýar.', 'Çeşme: Namaz wagty APK. Şäher düzedişi hasaplama modelidir; ýerli tertip bilen barlaň.'),
 ('Используется региональное расписание из исходной базы Namaz wagty.', 'Источник: база Namaz wagty APK. Поправки по городам расчётные, не утверждённый местный календарь.'),
 ('The regional timetable from the original Namaz wagty database is used.', 'Source: Namaz wagty APK. City adjustments are estimates, not an approved local calendar.'),
 ('Orijinal Namaz wagty veritabanındaki bölgesel takvim kullanılıyor.', 'Kaynak: Namaz wagty APK. Şehir düzeltmeleri hesaplamadır; onaylı yerel takvim değildir.')
]:
    assert old in s, old
    s = s.replace(old, new)
# Long method labels must not squeeze into an unscrollable row.
start = s.index('private fun PrayerCalculationSettingsCard(')
end = s.index('@Composable', start)
block = s[start:end]
assert block.count('Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {') == 1
block = block.replace('Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {',
                      'Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {', 1)
s = s[:start] + block + s[end:]
# Honest, expandable source label below the existing prayer card (no duplicate times).
replace_once('        item { Text(text.quickAccess, fontSize=19.sp, fontWeight=FontWeight.SemiBold, color=Ink) }',
             '        item { CityScheduleSourceInfo(city, language) }\n        item { Text(text.quickAccess, fontSize=19.sp, fontWeight=FontWeight.SemiBold, color=Ink) }')
for tag in ['Zikir we dogalar', 'Зикр и дуа', 'Dhikr & Duas', 'Zikir ve dualar']:
    s = s.replace(tag + ' • v1.2', tag + ' • v2.1 Solar')
s += '''

// CITY_SOLAR_INTEGRATION_V1
internal fun flaggedPrayerScheduleKeys(context: Context, date: LocalDate, city: City): Set<String> =
    if (getPrayerCalculationMode(context) == PrayerCalculationMode.MUFTIATE_TKM) {
        CitySolarSchedule.forCity(date, city).flaggedKeys
    } else emptySet()

@Composable
private fun CityScheduleSourceInfo(city: City, language: AppLanguage) {
    val context = LocalContext.current
    if (getPrayerCalculationMode(context) != PrayerCalculationMode.MUFTIATE_TKM) return
    val date = LocalDate.now(TurkmenistanZone)
    val report = remember(city, date) { CitySolarSchedule.forCity(date, city) }
    var expanded by remember { mutableStateOf(false) }
    val warning = report.flaggedKeys.isNotEmpty()
    val short = if (report.isReferencePoint) {
        localized(language, "Namaz wagty tablisasy", "Таблица Namaz wagty", "Namaz wagty table", "Namaz wagty tablosu")
    } else {
        localized(language, "Şäher boýunça hasaplama", "Расчёт по городу", "City-adjusted schedule", "Şehre göre hesaplama")
    }
    Text(
        (if (warning) "⚠ " else "ⓘ ") + short + " · " +
            localized(language, "Jikme-jik", "Подробнее", "Details", "Ayrıntılar"),
        modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 6.dp),
        color = Green,
        fontSize = 12.sp
    )
    if (expanded) {
        val morning = java.lang.String.format(java.util.Locale.ROOT, "%+.2f", report.morningShift)
        val evening = java.lang.String.format(java.util.Locale.ROOT, "%+.2f", report.eveningShift)
        val detail = localized(
            language,
            "Çeşme: Namaz wagty APK-daky sebit tablisasy. Tablisada ýyl we resmi tassyklama görkezilmeýär.\\n\\nŞäher: ${city.name}\\nSene: $date\\nÇak edilýän daýanç şäher: ${report.anchor.name}\\nIrdenki tapawut: $morning min\\nAgşamky tapawut: $evening min\\nÖýle: ${report.times.dhuhr} (üýtgemeýär).\\n\\nŞäher düzedişi hasaplama modelidir. Ýerli tertip bilen barlaň. Ertir bu modelde oraza başlamak wagty diýip görkezilmeýär.",
            "Источник: региональная таблица из Namaz wagty APK. В таблице не указаны год и официальное утверждение.\\n\\nГород: ${city.name}\\nДата: $date\\nПредполагаемый опорный город: ${report.anchor.name}\\nУтренняя поправка: $morning мин\\nВечерняя поправка: $evening мин\\nÖýle: ${report.times.dhuhr} (фиксированно).\\n\\nПоправки по городам — расчётная модель. Сверяйте с местным расписанием. Ertir здесь не обозначает подтверждённую границу начала поста.",
            "Source: regional timetable from Namaz wagty APK. The table has no year or approval metadata.\\n\\nCity: ${city.name}\\nDate: $date\\nAssumed reference city: ${report.anchor.name}\\nMorning shift: $morning min\\nEvening shift: $evening min\\nDhuhr: ${report.times.dhuhr} (fixed).\\n\\nCity adjustments are a model. Check your local calendar. Ertir is not presented as a verified fasting start time.",
            "Kaynak: Namaz wagty APK bölge tablosu. Tabloda yıl veya onay bilgisi yok.\\n\\nŞehir: ${city.name}\\nTarih: $date\\nVarsayılan referans şehir: ${report.anchor.name}\\nSabah farkı: $morning dk\\nAkşam farkı: $evening dk\\nÖğle: ${report.times.dhuhr} (sabit).\\n\\nŞehir düzeltmeleri bir modeldir. Yerel takvimle kontrol edin. Ertir, doğrulanmış oruç başlangıcı olarak sunulmaz."
        )
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text(short) },
            text = {
                LazyColumn(Modifier.height(360.dp)) {
                    item { Text(detail) }
                    if (warning) {
                        item {
                            Text(
                                localized(language,
                                    "⚠ Çeşmede şübheli ýazgy bar. Degişli bildiriş wagtlaýyn goýulmaýar. Ýerli tertip bilen barlaň.",
                                    "⚠ В исходной строке обнаружена аномалия. Уведомление для сомнительного времени не назначается. Проверьте местный календарь.",
                                    "⚠ Source row contains an anomaly. Alerts for the flagged time are not scheduled. Check the local calendar.",
                                    "⚠ Kaynak satırda anormallik var. Şüpheli vakit için bildirim planlanmaz. Yerel takvimi kontrol edin."),
                                modifier = Modifier.padding(top = 14.dp), color = Green
                            )
                            Text(report.flaggedKeys.joinToString(", "), fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { expanded = false }) {
                    Text(localized(language, "Ýap", "Закрыть", "Close", "Kapat"))
                }
            }
        )
    }
}
'''
# Dhikr/Tasbih and the existing GPS function must be byte-for-byte preserved.
for name, stop in [('DhikrScreen', None), ('TasbihScreen', None), ('detectNearestCity', None)]:
    def body(text, name):
        m = re.search(r'(?:private |internal )?fun ' + name + r'\(', text)
        assert m, name
        begin = text.index('{', m.start()); depth = 0
        for i in range(begin, len(text)):
            if text[i] == '{': depth += 1
            elif text[i] == '}':
                depth -= 1
                if depth == 0: return text[m.start():i+1]
        raise AssertionError(name)
    assert body(original, name) == body(s, name), f'Protected function changed: {name}'
path.write_text(s, encoding='utf-8')

schedule_path = root / 'MuftiateSchedule.kt'
t = schedule_path.read_text(encoding='utf-8')
m = re.search(r'COMPRESSED_DATA = "([^"]+)"', t)
assert m
raw = zlib.decompress(base64.b64decode(m.group(1)))
assert hashlib.sha256(raw).hexdigest() == '76cc334fe833469f0360493c06839b951809ba2dc59ae4cf209a0c1fe3712497'
# java.util.Base64 is available at minSdk 26, and permits real JVM unit testing.
t = t.replace('import android.util.Base64', 'import java.util.Base64')
t, n = re.subn(r'Base64\.decode\(COMPRESSED_DATA,\s*Base64\.(?:DEFAULT|NO_WRAP)\)', 'Base64.getDecoder().decode(COMPRESSED_DATA)', t)
assert n == 1, 'Base64 call did not match'
schedule_path.write_text(t, encoding='utf-8')

reminder_path = root / 'PrayerReminder.kt'
r = reminder_path.read_text(encoding='utf-8')
old = '        val times = calculatePrayerTimesWithContext(context, date, city)'
assert r.count(old) == 1
r = r.replace(old, old + '\n        val flaggedKeys = flaggedPrayerScheduleKeys(context, date, city)', 1)
old = '            if (!preferences.getBoolean(NotificationKeys[prayerIndex], true)) return@forEachIndexed'
assert r.count(old) == 1
r = r.replace(old, old + '\n            if (NotificationKeys[prayerIndex] in flaggedKeys) return@forEachIndexed', 1)
reminder_path.write_text(r, encoding='utf-8')

gradle = Path('app/build.gradle'); g = gradle.read_text()
g = re.sub(r'versionCode\s+\d+', 'versionCode 2001', g, count=1)
g = re.sub(r'versionName\s+"[^"]+"', 'versionName "2.1-solar"', g, count=1)
g = g.replace('dependencies {', "dependencies {\n    testImplementation 'junit:junit:4.13.2'", 1)
gradle.write_text(g)
# Generate a plain-data fixture without initializing Android/Compose MainActivityKt.
city_source = re.search(r'internal val Cities = listOf\((.*?)\n\)', original, re.S).group(1)
fixture = root.parents[4] / 'test/java/com/msterjime/zikirdua/CityFixtures.kt'
# root.parents[4] is app/src; tests belong in app/src/test.
fixture = Path('app/src/test/java/com/msterjime/zikirdua/CityFixtures.kt')
fixture.parent.mkdir(parents=True, exist_ok=True)
fixture.write_text('package com.msterjime.zikirdua\n\ninternal val TestCities = listOf(' + city_source + '\n)\n', encoding='utf-8')
print('Integrated: shared city schedule, honest source labels, fixed Oyle, anomaly guards, version 2.1-solar')
