package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame

class EmptyInputProvider : InputProvider {
    val frame = Frame(100, 100, 8, 3)

    override fun open() {

    }

    override fun read(): Frame {
        return frame
    }

    override fun close() {

    }

}