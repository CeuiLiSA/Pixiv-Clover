package ceui.lisa.slinky.ui

import androidx.annotation.MainThread
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.ResponseCache
import ceui.lisa.slinky.core.SLAdapter
import ceui.lisa.slinky.core.setUpLoadingState
import ceui.lisa.slinky.databinding.ItemRecommendUserBinding
import ceui.lisa.slinky.databinding.ItemSquareTagBinding
import ceui.lisa.slinky.databinding.ItemUserHorizontalBinding
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.models.TrendingTag
import ceui.lisa.slinky.models.UserPreview
import ceui.lisa.slinky.models.UserPreviewResponse
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.styles.ProgressImageButton
import ceui.lisa.slinky.utils.toGlideUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@BindingAdapter("showProgress")
fun ProgressImageButton.binding_showProgress(boolean: Boolean) {
    showProgress(boolean)
}

class UserHorizontalHolder(val userPreview: UserPreview) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return userPreview.user?.id == (other as? UserHorizontalHolder)?.userPreview?.user?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return userPreview == (other as? UserHorizontalHolder)?.userPreview
    }
}


@ItemHolder(UserHorizontalHolder::class)
class UserHorizontalViewHolder(aa: ItemUserHorizontalBinding) :
    SlinkyViewHolder<ItemUserHorizontalBinding, UserHorizontalHolder>(aa) {

    override fun onBindViewHolder(item: UserHorizontalHolder) {
        super.onBindViewHolder(item)
        binding.user = ObjectPool.get(item.userPreview.user?.id ?: 0L)
        binding.userFrame.setOnClick {
            item.userPreview.user?.let { user ->
                it.findActionReceiverOrNull<UserAction>()?.onClickUser(user)
            }
        }
    }
}

class RecommendUserHolder(val valueContent: ValueContent<UserPreviewResponse>) : SlinkyItem() {

    val loadState: LiveData<LoadState> = valueContent.loadState
    val items: LiveData<List<UserPreview>> = valueContent.result.map { it.user_previews }

}


@ItemHolder(RecommendUserHolder::class)
class RecommendUserViewHolder(aa: ItemRecommendUserBinding) :
    SlinkyViewHolder<ItemRecommendUserBinding, RecommendUserHolder>(aa) {

    override fun onBindViewHolder(item: RecommendUserHolder) {
        super.onBindViewHolder(item)
        if (binding.listView.adapter == null) {
            binding.includeItemLoading.setUpLoadingState(item.loadState, lifecycleOwner) {
                item.valueContent.refresh()
            }
            binding.listView.setUpGridlayoutManager(context)
            val adapter = SLAdapter(lifecycleOwner)
            binding.listView.adapter = adapter
            item.items.observe(lifecycleOwner) {
                adapter.submitList(it.map { userPreview ->
                    UserHorizontalHolder(userPreview)
                })
            }
        }
    }
}

@MainThread
inline fun <reified VM : ViewModel, ArgT1 : Any> Fragment.constructVM(
    crossinline arg1Producer: () -> ArgT1,
    noinline vmCtr: (ArgT1) -> VM
) = createViewModelLazy(VM::class, { this.viewModelStore }) {
    val frag = this
    object : AbstractSavedStateViewModelFactory(frag, null) {
        val arg1 = arg1Producer()

        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return vmCtr(arg1) as T
        }
    }
}


class SquareTagHolder(val trendingTag: TrendingTag) : SlinkyItem()


@ItemHolder(SquareTagHolder::class)
class SquareTagViewHolder(aa: ItemSquareTagBinding) :
    SlinkyViewHolder<ItemSquareTagBinding, SquareTagHolder>(aa) {

    override fun onBindViewHolder(item: SquareTagHolder) {
        super.onBindViewHolder(item)
        item.trendingTag.illust?.let { illust ->
            GlideApp.with(context)
                .load(illust.image_urls?.large?.toGlideUrl())
                .into(binding.imageView)
        }
        binding.root.setOnClick {
            item.trendingTag.tag?.let { name ->
                binding.imageView.findActionReceiverOrNull<TrendingTagAction>()?.searchTag(name)
            }
        }
        binding.root.setOnLongClickListener {
            binding.imageView.findActionReceiverOrNull<TrendingTagAction>()
                ?.onLongClickTrendingTag(item.trendingTag)
            true
        }
        binding.tagName.text = "#${item.trendingTag.translated_name ?: item.trendingTag.tag}"
    }
}