package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemSimpleUserBinding
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.Client

class SimpleUserListFragment :
    SlinkyListFragment(R.layout.fragment_slinky_list) {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel({ senderId }) { myselfId ->
        PixivListRepository(
            loader = { Client.appApi.getUserFollowingList(myselfId, BookmarkType.PUBLIC) },
            dataMapper = { UserPreviewHolder(it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }
}

class SimpleUserHolder(val user: LiveData<User>) : SlinkyItem()


@ItemHolder(SimpleUserHolder::class)
class SimpleUserViewHolder(aa: ItemSimpleUserBinding) :
    SlinkyViewHolder<ItemSimpleUserBinding, SimpleUserHolder>(aa) {

    override fun onBindViewHolder(item: SimpleUserHolder) {
        super.onBindViewHolder(item)
        binding.userSnapLayout.user = item.user
        binding.userSnapLayout.lifecycleOwner = lifecycleOwner
    }
}