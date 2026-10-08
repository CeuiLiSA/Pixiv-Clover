package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemSelectionBinding
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.models.AISettings
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.ui.dialog.showSpinner
import com.scwang.smart.refresh.header.FalsifyFooter
import kotlinx.coroutines.launch

class AISettingsRepository : CustomRepository<AISettingsFragment>() {

    private val aiSettings = MutableLiveData<AISettings>()
    override suspend fun suspendRefresh(
        fragment: AISettingsFragment
    ) {
        val resp = Client.appApi.getAISettings()
        aiSettings.value = resp
        holderList.value = listOf(
            SelectionHolder(
                fragment.getString(R.string.display_show),
                aiSettings.map { it.show_ai == true }).bindOnClick {
                updateAISettings(fragment, true)
            },
            SelectionHolder(
                fragment.getString(R.string.display_hide),
                aiSettings.map { it.show_ai != true }).bindOnClick {
                updateAISettings(fragment, false)
            },
        )
        refreshState.value = LoadState.LOADED(hasContent = true, hasNext = false)
    }

    private fun updateAISettings(fragment: AISettingsFragment, shouldShow: Boolean) {
        coroutineScope.launch {
            val spinner = fragment.showSpinner()
            try {
                val resp = Client.appApi.postAISettings(shouldShow)
                aiSettings.value = resp
            } catch (ex: Exception) {
                fragment.handleError(ex)
            } finally {
                spinner.end()
            }
        }
    }
}

class AISettingsFragment : SlinkyListFragment(R.layout.fragment_slinky_list) {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel { AISettingsRepository() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.ai_works_display_settings)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.refreshLayout.setRefreshFooter(FalsifyFooter(requireContext()))
    }
}

class SelectionHolder(val title: String, val isSelected: LiveData<Boolean>) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? SelectionHolder)?.title
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? SelectionHolder)?.title
    }
}

@ItemHolder(SelectionHolder::class)
class SelectionViewHolder(aa: ItemSelectionBinding) :
    SlinkyViewHolder<ItemSelectionBinding, SelectionHolder>(aa) {

    override fun onBindViewHolder(item: SelectionHolder) {
        super.onBindViewHolder(item)
        binding.viewModel = item
    }
}

