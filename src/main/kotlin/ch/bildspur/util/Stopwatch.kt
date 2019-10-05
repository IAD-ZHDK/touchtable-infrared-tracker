package ch.bildspur.util

class Stopwatch {
    private var startTime: Long = 0
    private var stopTime: Long = 0

    private var running = false

    fun start() {
        running = true
        startTime = millis()
    }

    fun stop() {
        stopTime = millis()
        running = false
    }

    fun elapsed(): Long {
        return if (running)
            millis() - startTime
        else
            stopTime - startTime
    }

    override fun toString(): String {
        val m = elapsed()

        val second = m / 1000 % 60
        val minute = m / (1000 * 60) % 60
        val hour = m / (1000 * 60 * 60) % 24

        return String.format("%02dh %02dm %02ds", hour, minute, second)
    }

    private fun millis(): Long {
        return System.currentTimeMillis()
    }
}