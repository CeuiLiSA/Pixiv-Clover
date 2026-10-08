package ceui.lisa.slinky.ui.task

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.UUID

abstract class SlinkyTask<T> {

    protected var cachedResult: T? = null

    val state = MutableLiveData(TaskState.Pending)

    open val taskName = UUID.randomUUID().toString()

    open val taskId: String = UUID.randomUUID().toString()

    abstract suspend fun action(): T

    var onComplete: ((T) -> Unit)? = null

    open fun onTaskComplete(objectT: T) {
        onComplete?.invoke(objectT)
    }

    var isExecutedAndFailed = false

    open fun cancel() {
        state.value = TaskState.Pending
    }
}

object TaskState {
    const val Pending = 0
    const val Running = 1
    const val Finished = 2
    const val Failed = 3
}

abstract class HumanReadableTask<T> : SlinkyTask<T>() {

    abstract fun executingTitle(context: Context): LiveData<String>

    abstract fun finishedTitle(context: Context): String
}