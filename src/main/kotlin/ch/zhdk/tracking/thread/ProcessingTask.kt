package ch.zhdk.tracking.thread

data class ProcessingTask(val block : () -> Unit, @Volatile var finished : Boolean = false)