package ch.bildspur.thread

class ProcessingInvoker {
    private val invokerLock = Object()
    private val tasks = mutableListOf<ProcessingTask>()

    fun addTask(task : ProcessingTask) {
        synchronized(invokerLock) {
            tasks.add(task)
        }
    }

    fun addTaskAndWait(task : ProcessingTask) {
        synchronized(invokerLock) {
            tasks.add(task)
        }

        while(!task.finished) {
            Thread.sleep(1)
        }
    }

    fun invokeTasks() {
        if(tasks.isEmpty())
            return

        // copy tasks
        val currentTasks = mutableListOf<ProcessingTask>()
        synchronized(invokerLock) {
            currentTasks.addAll(tasks)
        }

        // execute tasks
        currentTasks.forEach {
            it.block()
            it.finished = true
        }

        // remove tasks
        synchronized(invokerLock) {
            tasks.removeAll(currentTasks)
        }
    }
}