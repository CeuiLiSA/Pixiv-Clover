package ceui.lisa.slinky.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.DialogSubmittingBinding
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.models.AddCommentResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.ui.task.HumanReadableTask
import ceui.lisa.slinky.utils.SoundPlay
import kotlinx.coroutines.delay

class SubmittingDialog<T>(val task: HumanReadableTask<T>) :
    DialogFragment(R.layout.dialog_submitting) {

    companion object {
        const val TAG = "SubmittingDialog"
    }

    private val binding by viewBinding(DialogSubmittingBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.MercuryPrompt)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        task.executingTitle(context).observe(viewLifecycleOwner) { title ->
            binding.title.text = title
        }
        launchSuspend {
            binding.progressCircular.showProgress(true)
            try {
                val result = task.action()
                binding.progressCircular.showProgress(false)
                binding.progressCircular.isVisible = false
                SoundPlay.play(R.raw.download_fixed)
                delay(200L)
                binding.title.text = task.finishedTitle(context)
                binding.iconComplete.isVisible = true
                delay(200L)
                task.onTaskComplete(result)
            } catch (ex: Exception) {
                handleError(ex)
            } finally {
                dismissAllowingStateLoss()
            }
        }
    }
}

class CommentTask(
    private val content: String,
    private val illustId: Long,
    private val parentCommentId: Long? = null
) : HumanReadableTask<AddCommentResponse>() {

    override fun executingTitle(context: Context): LiveData<String> {
        return MutableLiveData(context.getString(R.string.task_submitting))
    }

    override fun finishedTitle(context: Context): String {
        return context.getString(R.string.task_submitted)
    }

    override suspend fun action(): AddCommentResponse {
        delay(200L)
        return Client.appApi.postComment(illustId, content, parentCommentId)
    }
}