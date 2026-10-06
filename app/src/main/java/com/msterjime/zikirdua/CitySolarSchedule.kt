package com.msterjime.zikirdua

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.*

/**
 * Reproduces the supplied regional timetable and transfers its sunrise/sunset
 * differences to a selected city. NOT an official Muftiate calculation method.
 * Reference coordinates are explicit modelling assumptions, not metadata from
 * the source database. Ertir is NOT asserted to be the beginning of fasting.
 * Source rows are never silently corrected; anomalous rows are flagged.
 * Solar geometry: Meeus/NOAA, centre altitude -0.833 degrees, UTC+5, no terrain.
 * https://gml.noaa.gov/grad/solcalc/calcdetails.html
 */
internal object CitySolarSchedule {
    data class Anchor(val city: City)
    data class SolarEvents(val sunriseMinutes: Double, val sunsetMinutes: Double)
    data class Result(
        val times: PrayerTimes,
        val anchor: City,
        val morningShift: Double,
        val eveningShift: Double,
        val isReferencePoint: Boolean,
        val flaggedKeys: Set<String>
    )

    val anchors = listOf(
        Anchor(City("Aşgabat", "Aşgabat", 37.9601, 58.3261)),
        Anchor(City("Balkan", "Balkanabat", 39.5108, 54.3671)),
        Anchor(City("Daşoguz", "Daşoguz", 41.8363, 59.9666)),
        Anchor(City("Lebap", "Türkmenabat", 39.0733, 63.5787)),
        Anchor(City("Mary", "Mary", 37.5928, 61.8303))
    )

    fun anchorFor(city: City): City = when (city.region) {
        "Aşgabat", "Ahal", "Arkadag" -> anchors[0].city
        "Balkan" -> anchors[1].city
        "Daşoguz" -> anchors[2].city
        "Lebap" -> anchors[3].city
        "Mary" -> anchors[4].city
        else -> error("No regional timetable for ${city.region}")
    }

    fun forCity(date: LocalDate, city: City): Result {
        val anchor = anchorFor(city)
        val source = requireNotNull(MuftiateSchedule.prayerTimes(date, anchor)) {
            "Regional timetable missing for ${anchor.name}, $date"
        }
        val reference = abs(city.latitude - anchor.latitude) < 0.000001 &&
            abs(city.longitude - anchor.longitude) < 0.000001
        val origin = solarEvents(date, anchor.latitude, anchor.longitude)
        val local = if (reference) origin else solarEvents(date, city.latitude, city.longitude)
        // Subtract unrounded events. Round only the final displayed prayer time.
        val morning = local.sunriseMinutes - origin.sunriseMinutes
        val evening = local.sunsetMinutes - origin.sunsetMinutes
        return Result(
            times = if (reference) source else source.copy(
                fajr = shift(source.fajr, morning),
                sunrise = shift(source.sunrise, morning),
                // Oyle is the fixed regional reading time, NOT solar noon.
                dhuhr = source.dhuhr,
                asr = shift(source.asr, evening),
                maghrib = shift(source.maghrib, evening),
                isha = shift(source.isha, evening)
            ),
            anchor = anchor,
            morningShift = morning,
            eveningShift = evening,
            isReferencePoint = reference,
            flaggedKeys = sourceIssues(source)
        )
    }

    private fun minutes(time: LocalTime): Int = time.hour * 60 + time.minute

    private fun shift(time: LocalTime, delta: Double): LocalTime {
        require(delta.isFinite()) { "Invalid solar adjustment" }
        val value = floor(minutes(time) + delta + 0.5).toInt()
        require(value in 0..1439) { "Adjusted time falls outside the local calendar day" }
        return LocalTime.of(value / 60, value % 60)
    }

    fun sourceIssues(times: PrayerTimes): Set<String> = buildSet {
        // These are database-consistency checks, NOT religious validity tests.
        if (abs(minutes(times.sunrise) - minutes(times.fajr) - 70) > 3) {
            add("fajr_notification")
            add("sunrise")
        }
        if (abs(minutes(times.maghrib) - minutes(times.asr) - 100) > 3) {
            add("asr_notification")
        }
        if (abs(minutes(times.isha) - minutes(times.maghrib) - 80) > 3) {
            add("isha_notification")
        }
    }

    private fun r(value: Double) = Math.toRadians(value)
    private fun d(value: Double) = Math.toDegrees(value)

    /** Declination in degrees; equation of time in minutes. */
    private fun sun(jd: Double): Pair<Double, Double> {
        val t = (jd - 2451545.0) / 36525.0
        val l0 = ((280.46646 + t * (36000.76983 + t * 0.0003032)) % 360.0 + 360.0) % 360.0
        val m = 357.52911 + t * (35999.05029 - 0.0001537 * t)
        val e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
        val center = sin(r(m)) * (1.914602 - t * (0.004817 + 0.000014 * t)) +
            sin(2 * r(m)) * (0.019993 - 0.000101 * t) + sin(3 * r(m)) * 0.000289
        val omega = 125.04 - 1934.136 * t
        val apparent = l0 + center - 0.00569 - 0.00478 * sin(r(omega))
        val seconds = 21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))
        val eps0 = 23 + (26 + seconds / 60) / 60
        val eps = eps0 + 0.00256 * cos(r(omega))
        val declination = d(asin(sin(r(eps)) * sin(r(apparent))))
        val y = tan(r(eps) / 2).pow(2)
        val equation = 4 * d(y * sin(2 * r(l0)) - 2 * e * sin(r(m)) +
            4 * e * y * sin(r(m)) * cos(2 * r(l0)) -
            0.5 * y * y * sin(4 * r(l0)) - 1.25 * e * e * sin(2 * r(m)))
        return declination to equation
    }

    fun solarEvents(date: LocalDate, latitude: Double, longitude: Double): SolarEvents {
        require(latitude.isFinite() && longitude.isFinite()) { "Non-finite coordinates" }
        require(latitude > -90 && latitude < 90 && longitude in -180.0..180.0) {
            "Coordinates outside valid range"
        }
        val jd0 = 2440587.5 + date.toEpochDay()
        val zoneMinutes = 300.0
        fun event(sign: Int): Double {
            var minutes = 720 - 4 * longitude + zoneMinutes + sign * 360
            repeat(8) {
                val (dec, eq) = sun(jd0 + (minutes - zoneMinutes) / 1440)
                val ratio = (sin(r(-0.833)) - sin(r(latitude)) * sin(r(dec))) /
                    (cos(r(latitude)) * cos(r(dec)))
                require(ratio in -1.0..1.0) { "No sunrise/sunset at these coordinates on $date" }
                val hourAngle = d(acos(ratio))
                minutes = 720 - 4 * longitude - eq + sign * 4 * hourAngle + zoneMinutes
            }
            return minutes
        }
        return SolarEvents(event(-1), event(1))
    }
}
