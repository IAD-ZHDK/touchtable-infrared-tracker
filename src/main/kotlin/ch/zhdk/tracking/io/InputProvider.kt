package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame

abstract class InputProvider {
    var isOpen : Boolean = false
        protected set

    open fun open() {
        isOpen = true
    }

    abstract fun read() : Frame

    open fun close() {
        isOpen = false
    }
}