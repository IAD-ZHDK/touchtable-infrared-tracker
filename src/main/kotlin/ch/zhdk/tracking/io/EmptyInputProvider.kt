package ch.zhdk.tracking.io

import ch.bildspur.util.TimeKeeper
import org.bytedeco.javacv.Frame

class EmptyInputProvider : InputProvider(100, 100) {
    val frame = Frame(width, height, 8, 3)

    override fun open() {
        super.open()
    }

    override fun read(): Frame {
        Thread.sleep(33)
        frame.timestamp = TimeKeeper.millis()
        return frame
    }

    override fun close() {
        super.close()
    }

}