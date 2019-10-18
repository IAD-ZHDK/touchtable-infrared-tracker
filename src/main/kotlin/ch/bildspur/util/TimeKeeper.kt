package ch.bildspur.util

import java.time.Instant
import java.time.temporal.ChronoUnit

object TimeKeeper {

    fun now() : Instant {
        return Instant.now()
    }

    fun millis() : Long {
        return now().truncatedTo(ChronoUnit.MILLIS).toEpochMilli()
    }

    fun micros() : Long {
        return now().truncatedTo(ChronoUnit.MICROS).toEpochMilli()
    }
}