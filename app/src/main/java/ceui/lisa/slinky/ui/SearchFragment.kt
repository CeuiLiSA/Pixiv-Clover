package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.Event
import ceui.lisa.slinky.databinding.FragmentSearchBinding
import ceui.lisa.slinky.db.ViewHistory
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.models.Comment
import ceui.lisa.slinky.models.Tag
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.RoomDB
import ceui.lisa.slinky.styles.ProgressTextButton
import ceui.lisa.slinky.ui.dialog.alertNotice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel : ViewModel() {

    val word = MutableLiveData<String>()
    val regularSortType = MutableLiveData<String>()
    val popularSortType = MutableLiveData<String>()
    val normalRefreshEvent = MutableLiveData<Event<Unit>>()
    val popularRefreshEvent = MutableLiveData<Event<Unit>>()
    val usersRefreshEvent = MutableLiveData<Event<Unit>>()

    val pendingReplyComment = MutableLiveData<Comment?>()


    val tagList = MutableLiveData<List<Tag>>()

    val inputDraft = MutableLiveData("")

}

class SearchFragment : NavFragment(R.layout.fragment_search) {

    private val binding by viewBinding(FragmentSearchBinding::bind)
    private val safeArgs: SearchFragmentArgs by navArgs()
    private val viewModel by viewModels<SearchViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.search)
        binding.viewModel = viewModel
        binding.search.setOnClick {
            performSearch()
        }
        viewModel.word.observe(viewLifecycleOwner) {
            binding.search.isEnabled = it.trim().isNotEmpty()
        }
        binding.clearText.setOnClick {
            binding.editText.setText("")
        }
        binding.idSearchIllust.setOnClick {
            idSearchIllust(it)
        }
        binding.idSearchUser.setOnClick {
            idSearchUser(it)
        }
        binding.parseLink.setOnClick {
            parseLink(it)
        }
        loadSearchHistory()
    }

    private fun loadSearchHistory() = launch {
        val histories = mutableListOf<ViewHistory>()
        withContext(Dispatchers.IO) {
            histories.addAll(RoomDB.db().historyDao().getHistoryByType(HistoryType.SEARCH_KEYWORD))
        }
        withContext(Dispatchers.Main) {
            if (histories.isNotEmpty()) {
                val tagList = histories.map {
                    Tag(it.objectJson, it.objectJson)
                }
                binding.tagsFlowView.setOnCellClickListner { cell, index ->
                    if (index < tagList.size) {
                        tagList[index].name?.let { name ->
                            searchTagImpl(name, true)
                        }
                    }
                }
                binding.tagsFlowView.setTags(tagList)
            }
        }
    }

    private fun idSearchIllust(sender: ProgressTextButton) {
        val word = viewModel.word.value ?: return
        launchSuspend {
            try {
                val id = word.toLong()
                showIllust(id, sender)
            } catch (ex: Exception) {
                alertNotice(message = getString(R.string.should_input_only_number))
            }
        }
    }

    private fun parseLink(sender: ProgressTextButton) {
        val word = viewModel.word.value ?: return
        launchSuspend {
            try {
                if (word.startsWith(ILLUST_URL_HEAD)) {
                    val id = word.substring(ILLUST_URL_HEAD.length).toLong()
                    showIllust(id, sender)
                } else if (word.startsWith(USER_URL_HEAD)) {
                    val id = word.substring(USER_URL_HEAD.length).toLong()
                    doSearchUser(id)
                }
            } catch (ex: Exception) {
                launch {
                    if (ex is NumberFormatException) {
                        alertNotice(message = getString(R.string.should_input_only_number))
                    } else {
                        handleError(ex)
                    }
                }
            }
        }
    }

    private fun idSearchUser(sender: ProgressTextButton) {
        val word = viewModel.word.value ?: return
        launchSuspend {
            try {
                sender.showProgress()
                val id = word.toLong()
                doSearchUser(id)
            } catch (ex: Exception) {
                launch {
                    if (ex is NumberFormatException) {
                        alertNotice(message = getString(R.string.should_input_only_number))
                    } else {
                        alertNotice(message = getString(R.string.user_not_found))
                    }
                }
            } finally {
                sender.hideProgress()
            }
        }
    }

    private suspend fun doSearchUser(id: Long) {
        val userResp = Client.appApi.user(id)
        val user = userResp.user
        if (user != null) {
            ObjectPool.update(user)
            onClickUserImpl(user)
        } else {
            alertNotice(message = getString(R.string.user_not_found))
        }
    }

    private fun performSearch() {
        val word = viewModel.word.value ?: return
        hideKeyboard()
        if (safeArgs.type == ViewPagerContentType.TYPE_SEARCH_KEY_WORD) {
            searchTagImpl(word, true)
        } else {
            searchNovelTagImpl(word)
        }
    }
}