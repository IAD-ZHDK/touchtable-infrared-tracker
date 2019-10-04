package ch.fhnw.exakt.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.nio.FloatBuffer


fun allocateDirectFloatBuffer(n: Int): FloatBuffer {
    return ByteBuffer.allocateDirect(n * java.lang.Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer()
}

fun allocateDirectIntBuffer(n: Int): IntBuffer {
    return ByteBuffer.allocateDirect(n * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer()
}