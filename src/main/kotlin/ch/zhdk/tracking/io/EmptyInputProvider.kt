package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame

class EmptyInputProvider : InputProvider(100, 100) {
    val frame = Frame(width, height, 8, 3)

    override fun open() {
        super.open()
    }

    override fun read(): Frame {
        return frame
    }

    override fun close() {
        super.close()
    }

}