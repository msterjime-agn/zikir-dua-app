#!/usr/bin/env python3
"""Integrate the standalone edition without modifying calculations or dhikr content."""
from pathlib import Path
import re, hashlib, xml.etree.ElementTree as ET

root = Path('app/src/main')
code = root / 'java/com/msterjime/zikirdua'
main = code / 'MainActivity.kt'
s = main.read_text(encoding='utf-8')
if '// STANDALONE_22_INTEGRATED' in s:
    print('Standalone integration already applied')
    raise SystemExit(0)
assert '// CITY_SOLAR_INTEGRATION_V1' in s, 'Solar source is required'
protected = {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in [code/'CitySolarSchedule.kt', code/'MuftiateSchedule.kt']}
original = s

def once(old, new):
    global s
    assert s.count(old) == 1, (old[:80], s.count(old))
    s = s.replace(old, new, 1)

# Keep all existing destinations, data and preferences; only shorten the nav label.
once('AppTab.PRAYER -> notificationSettingsTitle(language)',
     'AppTab.PRAYER -> localized(language, "Sazlamalar", "Настройки", "Settings", "Ayarlar")')
s = s.replace('• v2.1 Solar', '• v2.2')
once('        reschedulePrayerEvents(\n            context = context,',
     '        PrayerWidgets.updateAll(context)\n        reschedulePrayerEvents(\n            context = context,')
once('                    PrayerNotificationCard(language, refreshSchedules)',
     '                    PrayerWidgetSettingsCard(language)\n                    Spacer(Modifier.height(12.dp))\n                    PrayerNotificationCard(language, refreshSchedules)')
# Full two-by-two rows: no disappearing 30-minute button and no letter-by-letter wrapping.
a = s.index('@Composable\nprivate fun PrayerNotificationCard(')
b = s.index('@Composable\nprivate fun AzanSettingsCard(', a)
old = s[a:b]
assert 'items(listOf(5, 10, 15, 30))' in old
s = s[:a] + '''@Composable
private fun PrayerNotificationCard(language: AppLanguage, onSettingsChanged: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE) }
    var selectedMinutes by remember { mutableIntStateOf(preferences.getInt("reminder_minutes", 10)) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(localized(language, "Bildirişler", "Уведомления", "Notifications", "Bildirimler"),
            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
        Text(localized(language, "Öňünden duýdurmak", "Напомнить заранее", "Remind before prayer", "Önceden hatırlat"), color = Green)
        listOf(listOf(5, 10), listOf(15, 30)).forEach { minutes ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                minutes.forEach { minute ->
                    Button(onClick = {
                        selectedMinutes = minute
                        preferences.edit().putInt("reminder_minutes", minute).apply()
                        onSettingsChanged()
                    }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedMinutes == minute) Gold else Color.White,
                            contentColor = DeepGreen)) {
                        Text(minute.toString() + localized(language, " min", " мин", " min", " dk"), maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

''' + s[b:]
s += '\n// STANDALONE_22_INTEGRATED\n'
main.write_text(s, encoding='utf-8')

# Namespace is retained for compiled classes and resources. Only app identity changes.
build = Path('app/build.gradle')
g = build.read_text()
g, n = re.subn(r'applicationId\s+"[^"]+"', 'applicationId "com.msterjime.namazwagty.solar"', g)
assert n == 1
g = re.sub(r'versionCode\s+\d+', 'versionCode 2200', g, count=1)
g = re.sub(r'versionName\s+"[^"]+"', 'versionName "2.2"', g, count=1)
g = g.replace('        minSdk 26', '        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"\n        minSdk 26')
g = g.replace('dependencies {', "dependencies {\n    androidTestImplementation 'androidx.test:runner:1.6.2'\n    androidTestImplementation 'androidx.test.ext:junit:1.2.1'", 1)
build.write_text(g)
manifest = root / 'AndroidManifest.xml'
m = manifest.read_text()
m = re.sub(r'android:name="\.([A-Za-z][A-Za-z0-9]*)"', r'android:name="com.msterjime.zikirdua.\1"', m)
m = m.replace('android:label="NAMAZ WAGTY Zikir &amp; Dogalar"', 'android:label="@string/standalone_app_name"')
assert 'android:label="@string/standalone_app_name"' in m
receivers = '''
        <receiver android:name="com.msterjime.zikirdua.CompactPrayerWidget" android:exported="false" android:label="@string/widget_compact_title">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
                <action android:name="android.intent.action.TIME_SET" />
                <action android:name="android.intent.action.DATE_CHANGED" />
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
                <action android:name="android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED" />
            </intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/widget_prayer_compact_info" />
        </receiver>
        <receiver android:name="com.msterjime.zikirdua.FullPrayerWidget" android:exported="false" android:label="@string/widget_full_title">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/widget_prayer_full_info" />
        </receiver>
'''
m = m.replace('    </application>', receivers + '    </application>')
ET.fromstring(m)
manifest.write_text(m)

res = root / 'res'
for directory in ['layout', 'xml', 'drawable', 'values', 'values-ru', 'values-tr']:
    (res/directory).mkdir(exist_ok=True)
for suffix, title, compact, full, description in [
    ('', 'NAMAZ WAGTY 2.2', 'NAMAZ WAGTY · Compact', 'NAMAZ WAGTY · Prayer times', 'Prayer times using the saved city and calculation method'),
    ('-ru', 'NAMAZ WAGTY 2.2', 'NAMAZ WAGTY · Компактный', 'NAMAZ WAGTY · Времена намазов', 'Времена намазов для выбранного города и метода расчёта'),
    ('-tr', 'NAMAZ WAGTY 2.2', 'NAMAZ WAGTY · Küçük', 'NAMAZ WAGTY · Namaz vakitleri', 'Seçilen şehir ve hesaplama yöntemiyle namaz vakitleri')]:
    (res/('values'+suffix)/'standalone.xml').write_text(f'''<?xml version="1.0" encoding="utf-8"?>
<resources><string name="standalone_app_name">{title}</string><string name="widget_compact_title">{compact}</string><string name="widget_full_title">{full}</string><string name="widget_prayer_description">{description}</string></resources>''', encoding='utf-8')
for name, color, radius in [('dark','#173F35',24),('light','#F8F6EF',24),('selected','#C8A95B',12),('clear','#00000000',12)]:
    (res/'drawable'/f'widget_prayer_{name}.xml').write_text(f'''<shape xmlns:android="http://schemas.android.com/apk/res/android"><solid android:color="{color}"/><corners android:radius="{radius}dp"/></shape>''')
for compact in [True, False]:
    name = 'compact' if compact else 'full'
    height = 72 if compact else 180
    cells = 1 if compact else 2
    (res/'xml'/f'widget_prayer_{name}_info.xml').write_text(f'''<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
 android:minWidth="250dp" android:minHeight="{height}dp" android:minResizeWidth="250dp" android:minResizeHeight="{height}dp"
 android:targetCellWidth="4" android:targetCellHeight="{cells}" android:updatePeriodMillis="1800000"
 android:initialLayout="@layout/widget_prayer_{name}" android:previewLayout="@layout/widget_prayer_{name}"
 android:description="@string/widget_prayer_description" android:resizeMode="horizontal|vertical" android:widgetCategory="home_screen"/>''')

# RemoteViews-compatible XML only; no unsupported Compose views in a launcher.
header = '''<LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:gravity="center_vertical">
 <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
  <TextView android:id="@+id/widget_city" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="NAMAZ WAGTY" android:textColor="#FFFFFF" android:textStyle="bold" android:textSize="16sp" android:maxLines="1" android:ellipsize="end"/>
  <TextView android:id="@+id/widget_meta" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="—" android:textSize="11sp" android:textColor="#D1E4DB" android:maxLines="1" android:ellipsize="end"/>
 </LinearLayout>
 <TextView android:id="@+id/widget_refresh" android:layout_width="48dp" android:layout_height="48dp" android:gravity="center" android:text="↻" android:textSize="27sp" android:textColor="#E8CD85" android:contentDescription="Refresh"/>
</LinearLayout>'''
next_row = '''<LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:gravity="center_vertical">
 <TextView android:id="@+id/widget_next" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:maxLines="1" android:ellipsize="end" android:text="—" android:textSize="15sp" android:textStyle="bold" android:textColor="#E8CD85"/>
 <TextView android:id="@+id/widget_time" android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_marginStart="8dp" android:text="--:--" android:textSize="22sp" android:textStyle="bold" android:textColor="#FFFFFF"/>
</LinearLayout>'''
start = '''<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android" android:id="@+id/widget_root" android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical" android:padding="12dp" android:background="@drawable/widget_prayer_dark">'''
(res/'layout'/'widget_prayer_compact.xml').write_text(start + header + next_row + '</LinearLayout>', encoding='utf-8')
cells = ''
for i in range(5):
    cells += f'''<LinearLayout android:id="@+id/widget_cell{i}" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:gravity="center" android:paddingTop="8dp" android:paddingBottom="8dp">
 <TextView android:id="@+id/widget_n{i}" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="—" android:textSize="10sp" android:textColor="#D1E4DB" android:gravity="center" android:maxLines="1" android:ellipsize="end"/>
 <TextView android:id="@+id/widget_t{i}" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="--:--" android:textSize="14sp" android:textColor="#FFFFFF" android:textStyle="bold" android:gravity="center" android:maxLines="1" android:autoSizeTextType="uniform" android:autoSizeMinTextSize="10sp" android:autoSizeMaxTextSize="14sp" android:autoSizeStepGranularity="1sp"/>
</LinearLayout>'''
hint = '<TextView android:id="@+id/widget_hint" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="—" android:textSize="11sp" android:textColor="#D1E4DB"/>'
footer = '<TextView android:id="@+id/widget_footer" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginTop="8dp" android:text="—" android:textSize="10sp" android:textColor="#D1E4DB" android:maxLines="2" android:ellipsize="end"/>'
(res/'layout'/'widget_prayer_full.xml').write_text(start + header + hint + next_row + '<LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginTop="8dp">' + cells + '</LinearLayout>' + footer + '</LinearLayout>', encoding='utf-8')

for p in res.rglob('widget_prayer*.xml'):
    ET.fromstring(p.read_text())
for name, checksum in protected.items():
    assert hashlib.sha256((code/name).read_bytes()).hexdigest() == checksum, name
# Every originally defined Kotlin function remains. Only the notification card is replaced.
functions = re.findall(r'\bfun\s+(\w+)\s*\(', original)
for function in set(functions):
    assert len(re.findall(r'\bfun\s+' + function + r'\s*\(', s)) == functions.count(function), function
assert s[s.index('@Composable\nprivate fun DhikrScreen'):s.index('@Composable\nprivate fun TasbihScreen')] == original[original.index('@Composable\nprivate fun DhikrScreen'):original.index('@Composable\nprivate fun TasbihScreen')]
Path('STANDALONE_BUILD.txt').write_text('Application ID: com.msterjime.namazwagty.solar\nVersion: 2.2 (2200)\nIndependent installation. Does not replace or erase com.msterjime.zikirdua.\nCitySolarSchedule and MuftiateSchedule preserved byte-for-byte.\nTwo native widgets; same calculation method and city as app.\nPrivate signing keys must NEVER be committed to a public repository.\n')
print('Standalone identity, two widgets and settings integrated. Calculations and dhikr unchanged.')
