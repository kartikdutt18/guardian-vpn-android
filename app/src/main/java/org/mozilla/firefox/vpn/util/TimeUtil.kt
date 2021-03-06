package org.mozilla.firefox.vpn.util

import android.os.SystemClock
import android.util.TimeFormatException
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParsePosition
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TimeUtil {
    private val zoneUtc = TimeZone.getTimeZone("UTC")

    fun now(): Date {
        return Calendar.getInstance(zoneUtc).time
    }

    @Throws(TimeFormatException::class)
    fun parse(time: String, format: TimeFormat): Date {
        try {
            return format.parse(time)
        } catch (e: Exception) {
            throw TimeFormatException(e.message.orEmpty(), e)
        }
    }

    fun parseOrNull(time: String, format: TimeFormat): Date? {
        return try {
            parse(time, format)
        } catch (e: org.mozilla.firefox.vpn.util.TimeFormatException) {
            null
        }
    }
}

interface TimeFormat {
    fun parse(time: String): Date

    object Iso8601 : TimeFormat {
        override fun parse(time: String): Date {
            return ISO8601Utils.parse(time, ParsePosition(0))
        }
    }
}

class TimeFormatException(msg: String, cause: Throwable) : RuntimeException(msg, cause)

inline fun <T : Any> measureElapsedRealtime(unit: TimeUnit = TimeUnit.MILLISECONDS, block: () -> T): Pair<T, Long> {
    val ts = SystemClock.elapsedRealtime()
    return block() to unit.convert(SystemClock.elapsedRealtime() - ts, TimeUnit.MILLISECONDS)
}
