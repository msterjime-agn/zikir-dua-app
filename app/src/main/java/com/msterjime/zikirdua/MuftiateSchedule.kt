package com.msterjime.zikirdua

import android.util.Base64
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.LocalTime
import java.util.zip.InflaterInputStream

/**
 * Exact month/day prayer timetable extracted from the original Namaz wagty
 * assets/manage database. Five regional tables are stored in this order:
 * ag, bn, dz, lb, mr. Each day stores Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha
 * as unsigned 16-bit minute-of-day values.
 */
internal object MuftiateSchedule {
    private const val DAYS_PER_REGION = 366
    private const val PRAYERS_PER_DAY = 6
    private const val BYTES_PER_VALUE = 2
    private const val BYTES_PER_DAY = PRAYERS_PER_DAY * BYTES_PER_VALUE
    private const val BYTES_PER_REGION = DAYS_PER_REGION * BYTES_PER_DAY
    private const val EXPECTED_SIZE = BYTES_PER_REGION * 5

    private const val COMPRESSED_DATA = "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"

    private val data: ByteArray? by lazy {
        runCatching {
            val compressed = Base64.decode(COMPRESSED_DATA, Base64.NO_WRAP)
            InflaterInputStream(ByteArrayInputStream(compressed)).use { stream ->
                stream.readBytes().takeIf { it.size == EXPECTED_SIZE }
                    ?: error("Invalid Muftiate timetable size")
            }
        }.getOrNull()
    }

    fun prayerTimes(date: LocalDate, city: City): PrayerTimes? {
        val regionIndex = when (city.region) {
            "Aşgabat", "Arkadag", "Ahal" -> 0
            "Balkan" -> 1
            "Daşoguz" -> 2
            "Lebap" -> 3
            "Mary" -> 4
            else -> return null
        }

        // The source database is a 366-day month/day calendar, not a single-year
        // schedule. Mapping through leap-year 2024 preserves Feb 29 and keeps all
        // following month/day rows aligned in non-leap years too.
        val bytes = data ?: return null
        val dayIndex = LocalDate.of(2024, date.monthValue, date.dayOfMonth).dayOfYear - 1
        val baseOffset = regionIndex * BYTES_PER_REGION + dayIndex * BYTES_PER_DAY

        fun minuteAt(position: Int): Int {
            val offset = baseOffset + position * BYTES_PER_VALUE
            return ((bytes[offset].toInt() and 0xFF) shl 8) or
                (bytes[offset + 1].toInt() and 0xFF)
        }

        fun timeAt(position: Int): LocalTime {
            val minutes = minuteAt(position)
            return LocalTime.of((minutes / 60) % 24, minutes % 60)
        }

        return PrayerTimes(
            fajr = timeAt(0),
            sunrise = timeAt(1),
            dhuhr = timeAt(2),
            asr = timeAt(3),
            maghrib = timeAt(4),
            isha = timeAt(5)
        )
    }
}
