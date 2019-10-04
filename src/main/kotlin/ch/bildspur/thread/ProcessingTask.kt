package ch.bildspur.thread

data class ProcessingTask(val block : () -> Unit, @Volatile var finished : Boolean = false)