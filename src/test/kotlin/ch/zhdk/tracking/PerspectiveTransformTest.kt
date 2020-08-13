package ch.zhdk.tracking

import ch.bildspur.math.Float2
import ch.bildspur.util.TimeKeeper
import ch.zhdk.tracking.math.perspectiveTransform
import java.time.temporal.ChronoUnit

object PerspectiveTransformTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val tl =  Float2(0.1f, 0.1f)
        val tr =  Float2(0.7f, 0.3f)
        val br =  Float2(0.8f, 0.4f)
        val bl =  Float2(0.2f, 0.9f)

        val point = Float2(0.3f, 0.4f)

        val result = point.perspectiveTransform(tl, tr, br, bl)
        println(result)
    }
}