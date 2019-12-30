package com.dekaustubh.utils

import java.util.*

class TimeUtil {
    companion object {
        /**
         * Helper function to return current millis in [timeZone].
         */
        fun getCurrentMillis(timeZone: TimeZone): Long {
            val c = Calendar.getInstance()
            c.timeZone = timeZone
            return c.timeInMillis
        }

        /**
         * Helper function to return current millis in [timeZone].
         */
        fun getCurrentUtcMillis(): Long {
            val c = Calendar.getInstance()
            c.timeZone = TimeZone.getTimeZone("UTC")
            return c.timeInMillis
        }
    }
}