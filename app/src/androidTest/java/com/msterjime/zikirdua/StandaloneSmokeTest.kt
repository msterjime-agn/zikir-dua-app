package com.msterjime.zikirdua

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class StandaloneSmokeTest {
    @Test fun standaloneIdentityAndActivityLaunch() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        assertEquals("com.msterjime.namazwagty.solar", context.packageName)
        context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE).edit()
            .putString("location_mode", "manual").putString("language", "ru")
            .putString("city", "Köneürgenç").putString("prayer_calculation_mode", "MUFTIATE_TKM")
            .putBoolean("prayer_time_notification", false).putBoolean("azan_enabled", false).commit()
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = instrumentation.startActivitySync(intent)
        instrumentation.waitForIdleSync()
        assertNotNull(activity)
        assertFalse(activity.isFinishing)
        val dir = File(context.getExternalFilesDir(null), "smoke").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot()?.let { shot ->
            File(dir, "main-screen.png").outputStream().use { shot.compress(Bitmap.CompressFormat.PNG, 100, it) }
            shot.recycle()
        }
        instrumentation.runOnMainSync { activity.finish() }
    }

    @Test fun bothWidgetsAreRegistered() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val registered = AppWidgetManager.getInstance(context).installedProviders.map { it.provider }.toSet()
        assertTrue(registered.contains(ComponentName(context, CompactPrayerWidget::class.java)))
        assertTrue(registered.contains(ComponentName(context, FullPrayerWidget::class.java)))
    }

    @Test fun widgetsInflateForBothThemesAndAllLanguages() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val prefs = context.getSharedPreferences("zikir_dua_settings", Context.MODE_PRIVATE)
        val dir = File(context.getExternalFilesDir(null), "smoke").apply { mkdirs() }
        for (code in listOf("tm", "ru", "en", "tr")) for (light in listOf(false, true)) for (compact in listOf(false, true)) {
            prefs.edit().putString("city", "Köneürgenç").putString("language", code)
                .putString("prayer_calculation_mode", "MUFTIATE_TKM").putBoolean("widget_light", light).commit()
            instrumentation.runOnMainSync {
                val parent = FrameLayout(context)
                val view = PrayerWidgets.render(context, compact).apply(context, parent)
                assertEquals("Köneürgenç", view.findViewById<TextView>(R.id.widget_city).text.toString())
                assertTrue(view.findViewById<TextView>(R.id.widget_time).text.toString().matches(Regex("\\d{2}:\\d{2}")))
                if (!compact) {
                    val city = Cities.first { it.name == "Köneürgenç" }
                    val date = LocalDate.now(ZoneOffset.ofHours(5))
                    val times = calculatePrayerTimesWithContext(context, date, city)
                    assertEquals(times.dhuhr.format(DateTimeFormatter.ofPattern("HH:mm")), view.findViewById<TextView>(R.id.widget_t1).text.toString())
                }
                val density = context.resources.displayMetrics.density
                val width = (320 * density).toInt()
                val height = ((if (compact) 80 else 208) * density).toInt()
                view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
                view.layout(0, 0, width, height)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap))
                File(dir, "widget-$code-$light-$compact.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        }
        prefs.edit().putString("language", "tm").putBoolean("widget_light", false).commit()
    }
}
