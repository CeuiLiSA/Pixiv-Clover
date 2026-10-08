package ceui.lisa.slinky.ui

import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.databinding.ItemTagsBinding
import ceui.lisa.slinky.models.Tag

class TagsHolder(val tagList: List<Tag>) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return tagList == (other as? TagsHolder)?.tagList
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return tagList == (other as? TagsHolder)?.tagList
    }
}


@ItemHolder(TagsHolder::class)
class TagsViewHolder(aa: ItemTagsBinding) :
    SlinkyViewHolder<ItemTagsBinding, TagsHolder>(aa) {

    override fun onBindViewHolder(item: TagsHolder) {
        super.onBindViewHolder(item)
        binding.tagsFlowView.setOnCellClickListner { cell, index ->
            if (index < item.tagList.size) {
                item.tagList[index].name?.let { name ->
                    cell.findActionReceiverOrNull<TagAction>()?.searchTag(name)
                }
            }
        }
        binding.tagsFlowView.setTags(item.tagList)
    }
}