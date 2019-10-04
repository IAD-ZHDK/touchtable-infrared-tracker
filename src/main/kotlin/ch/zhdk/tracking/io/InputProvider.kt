package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame

interface InputProvider {
    fun open()

    fun read() : Frame

    fun close()
}