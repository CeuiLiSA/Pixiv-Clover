package ceui.lisa.slinky.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.ui.task.SlinkyTask
import ceui.lisa.slinky.ui.task.TaskState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object DownloadTaskManager {

    private val _runningTask = MutableLiveData<List<SlinkyTask<*>>>()
    val runningTask: LiveData<List<SlinkyTask<*>>> = _runningTask

    private suspend fun runTopTask() {
        val currentTaskList = (_runningTask.value ?: listOf()).toMutableList()

        val task = currentTaskList.firstOrNull()
        if (task != null && task.state.value != TaskState.Running) {
            task.state.value = TaskState.Running
            val isSuccess = try {
                task.action()
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
            currentTaskList.removeFirst()
            if (isSuccess) {
                task.state.value = TaskState.Finished
                delay(200L)
            } else {
                task.state.value = TaskState.Failed
                currentTaskList.add(task)
            }
            _runningTask.value = currentTaskList
            runTopTask()
        } else {

        }
    }

    fun start() {
        MainScope().launch {
            runTopTask()
        }
    }

    fun stop() {
        val currentTaskList = _runningTask.value ?: listOf()
        currentTaskList.forEach {
            it.cancel()
        }
    }


    fun addTask(task: SlinkyTask<*>) {
        val currentTaskList = (_runningTask.value ?: listOf()).toMutableList()
        currentTaskList.add(task)
        _runningTask.value = currentTaskList
    }
}