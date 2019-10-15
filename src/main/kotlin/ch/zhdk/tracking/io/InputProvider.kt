package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame

abstract class InputProvider(var width : Int = 0, var height : Int = 0) {
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