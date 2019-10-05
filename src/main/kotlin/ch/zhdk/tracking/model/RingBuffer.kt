package ch.zhdk.tracking.model

import java.lang.Integer.min

class RingBuffer<T>(val size : Int) {
    private data class RingBufferValue<T>(var value : T? = null)

    private val data = Array(size) { RingBufferValue<T>() }
    private var currentIndex = 0

    var elementCount = 0
        private set

    fun add(value : T) {
        data[currentIndex].value = value
        currentIndex = Math.floorMod(currentIndex + 1, size)
        elementCount = min(elementCount + 1, size)
    }

    fun clear() {
        currentIndex = 0
        elementCount = 0
    }

    operator fun get(index : Int) : T
    {
        val realIndex = Math.floorMod(currentIndex - index, elementCount)

        if(data[realIndex].value == null)
            throw Exception("Ringbuffer has no value on index $currentIndex!")

        return data[realIndex].value!!
    }
}