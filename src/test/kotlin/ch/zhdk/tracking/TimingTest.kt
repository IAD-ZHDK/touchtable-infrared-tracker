package ch.zhdk.tracking

import ch.bildspur.util.TimeKeeper
import java.time.temporal.ChronoUnit

object TimingTest {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Timing Test")

        (0 until 100).forEach {
            println("MILLI: ${TimeKeeper.millis()} ")
        }
    }
}