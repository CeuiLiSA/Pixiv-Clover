package ceui.lisa.slinky.core

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.slinky.FullScreenActivity
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentCommentListBinding
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.models.Comment
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.styles.ProgressTextButton
import ceui.lisa.slinky.ui.CommentTask
import ceui.lisa.slinky.ui.SearchViewModel
import ceui.lisa.slinky.ui.SubmittingDialog
import ceui.lisa.slinky.ui.UserAction
import ceui.lisa.slinky.ui.hideKeyboard
import ceui.lisa.slinky.ui.launchSuspend
import ceui.lisa.slinky.ui.onClickUserImpl
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.ui.viewBinding

class CMFragment : SlinkyListFragment(R.layout.fragment_comment_list),
    UserAction {

    private val safeArgs: CMFragmentArgs by navArgs()
    private val searchViewModel by viewModels<SearchViewModel>()
    private val binding by viewBinding(FragmentCommentListBinding::bind)
    private val repository by lazy {
        CMRepository(safeArgs.illustId, safeArgs.authorId)
    }
    private val viewModel by listViewModel { repository }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        ObjectPool.get<Illust>(safeArgs.illustId).observe(viewLifecycleOwner) {
            actionbarContent.title.value = it.title
        }
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
        binding.viewModel = searchViewModel
        searchViewModel.word.observe(viewLifecycleOwner) {
            binding.search.isEnabled = it.trim().isNotEmpty()
        }
        binding.clearText.setOnClick {
            binding.editText.setText("")
        }
        searchViewModel.pendingReplyComment.observe(viewLifecycleOwner) { comment ->
            if (comment != null) {
                binding.editText.hint = "回复@${comment.user.name}"
            } else {
                binding.editText.hint = "说点什么吧"
            }
        }
        val list = binding.listView
        binding.search.setOnClick {
            val word = searchViewModel.word.value ?: return@setOnClick
            hideKeyboard()
            val parentComment = searchViewModel.pendingReplyComment.value
            val task = CommentTask(content = word, illustId = safeArgs.illustId, parentComment?.id)
            task.onComplete = { resp ->
                binding.editText.setText("")
                if (parentComment != null) {
                    searchViewModel.pendingReplyComment.value = null
                }
                launchSuspend {
                    repository.onCommentSubmitted(parentComment, resp, list)
                }
            }
            val dialog = SubmittingDialog(task)
            dialog.show(childFragmentManager, SubmittingDialog.TAG)
        }
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }

    override fun onClickUser(user: User) {
        onClickUserImpl(user)
    }

    override fun onStart() {
        super.onStart()
        (activity as? FullScreenActivity)?.updateShouldAdjustKeyboard(true)
    }

    override fun onStop() {
        super.onStop()
        (activity as? FullScreenActivity)?.updateShouldAdjustKeyboard(false)
    }

    fun showReply(sender: ProgressTextButton, parentComment: Comment) {
        launchSuspend {
            try {
                sender.showProgress()
                repository.showReplyImpl(parentComment)
            } catch (ex: Exception) {
                handleError(ex)
            } finally {
                sender.hideProgress()
            }
        }
    }

    fun replayToComment(comment: Comment) {
        searchViewModel.pendingReplyComment.value = comment
        showKeyboard(binding.editText)
    }
}

fun Context.showKeyboard(editText: EditText?) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    editText?.requestFocus()
    imm?.showSoftInput(editText, InputMethodManager.HIDE_IMPLICIT_ONLY)
//    imm?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun Fragment.showKeyboard(editText: EditText?) {
    context?.showKeyboard(editText)
}