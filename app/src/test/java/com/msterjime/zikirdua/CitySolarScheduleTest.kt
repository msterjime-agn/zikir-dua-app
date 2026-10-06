package com.msterjime.zikirdua

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.Year
import java.io.File
import java.security.MessageDigest
import kotlin.math.*

class CitySolarScheduleTest {
    private fun fields(t: PrayerTimes) = listOf(t.fajr, t.sunrise, t.dhuhr, t.asr, t.maghrib, t.isha)
    private fun minutes(t: LocalTime) = t.hour * 60 + t.minute
    private fun days(year: Int) = (1..Year.of(year).length()).map { LocalDate.ofYearDay(year, it) }

    @Test fun allSourceBytesMatchUploadedApk() {
        val digest = MessageDigest.getInstance("SHA-256")
        CitySolarSchedule.anchors.forEach { anchor ->
            days(2024).forEach { date ->
                fields(requireNotNull(MuftiateSchedule.prayerTimes(date, anchor.city))).forEach { time ->
                    val m = minutes(time)
                    digest.update((m shr 8).toByte())
                    digest.update((m and 255).toByte())
                }
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        assertEquals("76cc334fe833469f0360493c06839b951809ba2dc59ae4cf209a0c1fe3712497", actual)
    }

    @Test fun referenceCitiesExactlyReproduceAll366SourceRows() {
        CitySolarSchedule.anchors.forEach { a ->
            days(2024).forEach { date ->
                val actual = CitySolarSchedule.forCity(date, a.city)
                assertEquals(MuftiateSchedule.prayerTimes(date, a.city), actual.times)
                assertTrue(actual.isReferencePoint)
                assertEquals(0.0, actual.morningShift, 0.0)
                assertEquals(0.0, actual.eveningShift, 0.0)
            }
        }
    }

    @Test fun everyCatalogCityWorksAcrossCommonAndLeapYears() {
        var checked = 0
        TestCities.forEach { city ->
            listOf(2024, 2026).forEach { year ->
                days(year).forEach { date ->
                    val r = CitySolarSchedule.forCity(date, city)
                    val source = requireNotNull(MuftiateSchedule.prayerTimes(date, r.anchor))
                    assertEquals(if (city.region == "Balkan") LocalTime.of(13, 40) else LocalTime.of(13, 30), r.times.dhuhr)
                    assertTrue(r.morningShift.isFinite())
                    assertTrue(r.eveningShift.isFinite())
                    assertTrue(abs(r.morningShift) < 90)
                    assertTrue(abs(r.eveningShift) < 90)
                    val sourceFields = fields(source)
                    val resultFields = fields(r.times)
                    resultFields.forEachIndexed { index, time ->
                        val delta = when (index) { 0, 1 -> r.morningShift; 2 -> 0.0; else -> r.eveningShift }
                        assertEquals(floor(minutes(sourceFields[index]) + delta + 0.5).toInt(), minutes(time))
                    }
                    assertTrue(resultFields.zipWithNext().all { (a, b) -> a.isBefore(b) })
                    checked++
                }
            }
        }
        assertEquals(TestCities.size * 731, checked)
        val out = File("build/reports/city-solar")
        out.mkdirs()
        File(out, "coverage.txt").writeText("$checked city-days checked (${TestCities.size} cities, 2024 and 2026).\nThis tests implementation, NOT independent local Muftiate accuracy.\n")
    }

    @Test fun koneurgenchSeptemberExampleAndSeasonalChanges() {
        val city = TestCities.single { it.name == "Köneürgenç" }
        val r = CitySolarSchedule.forCity(LocalDate.of(2026, 9, 18), city)
        assertEquals(listOf("05:35", "06:45", "13:30", "17:34", "19:14", "20:34"), fields(r.times).map { it.toString() })
        assertFalse(r.isReferencePoint)
        assertEquals(3.10, r.morningShift, 0.03)
        assertEquals(3.39, r.eveningShift, 0.03)
        val winter = CitySolarSchedule.forCity(LocalDate.of(2026, 1, 1), city)
        val summer = CitySolarSchedule.forCity(LocalDate.of(2026, 6, 21), city)
        assertTrue(winter.morningShift > summer.morningShift + 2)
        assertTrue(summer.eveningShift > winter.eveningShift + 2)
    }

    @Test fun february29DoesNotShiftMarchInCommonYears() {
        val city = CitySolarSchedule.anchors[2].city
        assertNotNull(CitySolarSchedule.forCity(LocalDate.of(2024, 2, 29), city))
        assertEquals(CitySolarSchedule.forCity(LocalDate.of(2024, 3, 1), city).times,
            CitySolarSchedule.forCity(LocalDate.of(2026, 3, 1), city).times)
    }

    @Test fun suspectSourceRowsArePreservedAndFlagged() {
        val city = CitySolarSchedule.anchors[1].city
        val march = CitySolarSchedule.forCity(LocalDate.of(2026, 3, 31), city)
        assertEquals(LocalTime.of(20, 9), march.times.isha)
        assertTrue("isha_notification" in march.flaggedKeys)
        val december = CitySolarSchedule.forCity(LocalDate.of(2026, 12, 31), city)
        assertTrue("fajr_notification" in december.flaggedKeys)
        assertTrue(CitySolarSchedule.forCity(LocalDate.of(2026, 9, 18), city).flaggedKeys.isEmpty())
    }

    @Test fun rejectInvalidCoordinatesAndUnknownRegions() {
        try {
            CitySolarSchedule.solarEvents(LocalDate.of(2026, 1, 1), Double.NaN, 59.0)
            fail("Non-finite latitude accepted")
        } catch (_: IllegalArgumentException) { }
        try {
            CitySolarSchedule.forCity(LocalDate.of(2026, 1, 1), City("Unknown", "Unknown", 41.0, 59.0))
            fail("Unknown region silently fell back")
        } catch (_: IllegalStateException) { }
    }

    @Test fun writeExamplesForUser() {
        val out = File("build/reports/city-solar")
        out.mkdirs()
        val header = "date,city,anchor,fajr,sunrise,dhuhr,asr,maghrib,isha,morning_shift_minutes,evening_shift_minutes,flags\n"
        val body = StringBuilder(header)
        listOf(LocalDate.of(2026, 9, 18), LocalDate.of(2026, 10, 6), LocalDate.of(2024, 2, 29)).forEach { date ->
            TestCities.forEach { city ->
                val r = CitySolarSchedule.forCity(date, city)
                body.append(listOf(date, city.name, r.anchor.name).joinToString(","))
                body.append("," + fields(r.times).joinToString(","))
                body.append(",${r.morningShift},${r.eveningShift},${r.flaggedKeys.joinToString(";")}\n")
            }
        }
        File(out, "examples.csv").writeText(body.toString())
    }
}
