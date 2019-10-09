package ch.bildspur.timer

class ElapsedTimer(var duration : Long = 0) {
    var lastTimestamp = millis()
        private set

    fun elapsed() : Boolean {
        val m = millis()
        val result = (m - lastTimestamp) >= duration
        if(result)
            lastTimestamp = m
        return result
    }

    fun reset() {
        lastTimestamp = millis()
    }

    private fun millis() : Long
    {
        return System.currentTimeMillis()
    }
}