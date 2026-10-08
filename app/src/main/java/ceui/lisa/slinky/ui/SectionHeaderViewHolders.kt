package ceui.lisa.slinky.ui

import androidx.core.view.isVisible
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.databinding.ItemRedSectionHeaderBinding
import ceui.lisa.slinky.databinding.ItemSectionHeaderBinding

class SectionHeaderHolder(val title: String, val type: Int = 0, val seeMoreString: String? = null) :
    SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? SectionHeaderHolder)?.title
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? SectionHeaderHolder)?.title &&
                type == (other as? SectionHeaderHolder)?.type &&
                seeMoreString == (other as? SectionHeaderHolder)?.seeMoreString
    }
}


@ItemHolder(SectionHeaderHolder::class)
class SectionHeaderViewHolder(aa: ItemSectionHeaderBinding) :
    SlinkyViewHolder<ItemSectionHeaderBinding, SectionHeaderHolder>(aa) {

    override fun onBindViewHolder(item: SectionHeaderHolder) {
        super.onBindViewHolder(item)
        binding.title.text = item.title
        binding.seeMore.text = item.seeMoreString
        binding.seeMore.isVisible = item.type != 0
        binding.seeMore.setOnClick {
            it.findActionReceiverOrNull<SeeMoreAction>()?.seeMore(item.type)
        }
    }
}


class RedSectionHeaderHolder(
    val title: String,
    val type: Int = 0,
    val seeMoreString: String? = null
) : SlinkyItem() {


    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? RedSectionHeaderHolder)?.title
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? RedSectionHeaderHolder)?.title &&
                type == (other as? RedSectionHeaderHolder)?.type &&
                seeMoreString == (other as? RedSectionHeaderHolder)?.seeMoreString
    }
}


@ItemHolder(RedSectionHeaderHolder::class)
class RedSectionHeaderViewHolder(aa: ItemRedSectionHeaderBinding) :
    SlinkyViewHolder<ItemRedSectionHeaderBinding, RedSectionHeaderHolder>(aa) {

    override fun onBindViewHolder(item: RedSectionHeaderHolder) {
        super.onBindViewHolder(item)
        binding.title.text = item.title
        binding.seeMore.text = item.seeMoreString
        binding.seeMore.isVisible = item.type != 0
        binding.seeMore.setOnClick {
            it.findActionReceiverOrNull<SeeMoreAction>()?.seeMore(item.type)
        }
    }
}