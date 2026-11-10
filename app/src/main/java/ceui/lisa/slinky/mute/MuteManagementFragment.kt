package ceui.lisa.slinky.mute

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemTabBinding
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import ceui.lisa.slinky.ui.TabSectionHolder
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import ceui.lisa.slinky.ui.launch
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.ui.viewBinding

class MuteManagementRepository : CustomRepository<MuteManagementFragment>() {

    override suspend fun suspendRefresh(fragment: MuteManagementFragment) {
        val records = MuteStore.allRecords()
        holderList.value = buildHolders(fragment, records)
        refreshState.value = LoadState.LOADED(hasContent = records.isNotEmpty(), hasNext = false)
    }

    private fun buildHolders(
        fragment: MuteManagementFragment,
        records: List<MuteRecord>
    ): List<SlinkyItem> {
        val sections = listOf(
            MuteType.TAG to R.string.muted_tags,
            MuteType.USER to R.string.muted_users,
            MuteType.ILLUST to R.string.muted_illusts,
            MuteType.NOVEL to R.string.muted_novels,
        )
        val grouped = records.groupBy { it.type }
        return buildList {
            sections.forEach { (type, titleRes) ->
                val sectionRecords = grouped[type].orEmpty()
                if (sectionRecords.isNotEmpty()) {
                    add(TabSectionHolder(fragment.getString(titleRes)))
                    sectionRecords.forEach { add(MuteRecordHolder(it)) }
                }
            }
        }
    }
}

class MuteManagementFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel { MuteManagementRepository() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.mute_management)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }

    fun unmute(record: MuteRecord) {
        launch {
            val name = record.displayLabel()
            if (alertYesOrCancel(message = getString(R.string.unmute_confirm, name))) {
                MuteStore.unmute(record.type, record.key)
                viewModel.refresh(RefreshHint.initialLoad(), this)
            }
        }
    }
}

private fun MuteRecord.displayLabel(): String {
    return if (type == MuteType.TAG) "#$displayName" else displayName.ifEmpty { key }
}

class MuteRecordHolder(val record: MuteRecord) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        val otherRecord = (other as? MuteRecordHolder)?.record ?: return false
        return record.type == otherRecord.type && record.key == otherRecord.key
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return record == (other as? MuteRecordHolder)?.record
    }
}

@ItemHolder(MuteRecordHolder::class)
class MuteRecordViewHolder(aa: ItemTabBinding) :
    SlinkyViewHolder<ItemTabBinding, MuteRecordHolder>(aa) {

    override fun onBindViewHolder(item: MuteRecordHolder) {
        super.onBindViewHolder(item)
        binding.title.text = item.record.displayLabel()
        binding.rightTitle.isVisible = true
        binding.rightTitle.text = context.getString(R.string.unmute)
        binding.tabRoot.setOnClick {
            it.findFragment<MuteManagementFragment>().unmute(item.record)
        }
    }
}
