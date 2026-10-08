package ceui.lisa.slinky.ui

import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.databinding.CellTaskBinding
import ceui.lisa.slinky.ui.task.DownloadTask
import ceui.lisa.slinky.ui.task.TaskState

class TaskHolder(val downloadTask: DownloadTask) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return downloadTask.taskName == (other as? TaskHolder)?.downloadTask?.taskName
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return downloadTask.taskName == (other as? TaskHolder)?.downloadTask?.taskName
    }
}


@ItemHolder(TaskHolder::class)
class TaskViewHolder(aa: CellTaskBinding) : SlinkyViewHolder<CellTaskBinding, TaskHolder>(aa) {

    override fun onBindViewHolder(item: TaskHolder) {
        super.onBindViewHolder(item)
        binding.task = item.downloadTask
    }
}


@BindingAdapter("variableProgress")
fun ProgressBar.binding_set_variable_progress(progressValue: Int?) {
    progress = progressValue ?: 0
}


@BindingAdapter("taskState")
fun TextView.binding_set_task_state(taskState: Int?) {
    if (taskState != null) {
        when (taskState) {
            TaskState.Pending -> {
                text = "等待中"
            }

            TaskState.Running -> {
                text = "执行中"
            }

            TaskState.Finished -> {
                text = "已完成"
            }

            TaskState.Failed -> {
                text = "任务失败"
            }
        }
    }
}
