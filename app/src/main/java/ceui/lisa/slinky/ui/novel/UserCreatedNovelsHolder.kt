package ceui.lisa.slinky.ui.novel

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.SLAdapter
import ceui.lisa.slinky.core.setUpLoadingState
import ceui.lisa.slinky.databinding.ItemUserNovelsBinding
import ceui.lisa.slinky.models.NovelResponse
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import kotlin.math.min


class UserCreatedNovelsHolder(
    val novelResponse: LiveData<NovelResponse>,
    val loadState: LiveData<LoadState>,
    val refreshBlock: () -> Unit
) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return novelResponse.value == (other as? UserCreatedNovelsHolder)?.novelResponse?.value
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return novelResponse.value == (other as? UserCreatedNovelsHolder)?.novelResponse?.value
    }
}


@ItemHolder(UserCreatedNovelsHolder::class)
class UserNovelViewHolder(aa: ItemUserNovelsBinding) :
    SlinkyViewHolder<ItemUserNovelsBinding, UserCreatedNovelsHolder>(aa) {

    override fun onBindViewHolder(item: UserCreatedNovelsHolder) {
        super.onBindViewHolder(item)
        binding.includeItemLoading.setUpLoadingState(item.loadState, lifecycleOwner) {
            item.refreshBlock.invoke()
        }
        val adapter = SLAdapter(lifecycleOwner)
        binding.userNovels.adapter = adapter
        binding.userNovels.layoutManager = LinearLayoutManager(context)
        item.novelResponse.observe(lifecycleOwner) {
            val novelList = it.novels
            if (novelList.isNotEmpty()) {
                adapter.submitList(
                    novelList.subList(0, min(5, novelList.size)).map { novel ->
                        NovelHolder(novel)
                    }
                )
            }
        }
    }
}