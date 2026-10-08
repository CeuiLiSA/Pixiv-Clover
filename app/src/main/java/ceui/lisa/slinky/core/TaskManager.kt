package ceui.lisa.slinky.core

import ceui.lisa.slinky.ui.task.SlinkyTask
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object TaskManager {

    private val taskList = mutableListOf<SlinkyTask<*>>()
    private var isProcessingTask = false

    fun start() {
        if (isProcessingTask) {
            return
        }

        isProcessingTask = true
        taskList.forEach { it.isExecutedAndFailed = false }
        MainScope().launch {
            loop()
        }
    }

    private suspend fun loop() {
        val task = taskList.firstOrNull { !it.isExecutedAndFailed }
        if (task != null) {
            taskList.remove(task)
            try {
                task.action()
            } catch (ex: Exception) {
                ex.printStackTrace()
                task.isExecutedAndFailed = true
                taskList.add(task)
            }
            loop()
        } else {
            isProcessingTask = false
        }
    }

    fun addTask(task: SlinkyTask<*>) {
        if (taskList.any { it.taskId == task.taskId }) {
            return
        }

        taskList.add(task)
    }
}