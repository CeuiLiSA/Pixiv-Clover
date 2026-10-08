package ceui.lisa.slinky.ui

import androidx.fragment.app.findFragment
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.core.SLAdapter
import ceui.lisa.slinky.databinding.ItemIllustHorizontalListBinding
import ceui.lisa.slinky.databinding.ItemIllustSquareBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.models.WebIllust
import ceui.lisa.slinky.utils.toGlideUrl
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.Serializable

data class OneLine(
    val title: String? = null,
    val displayIllust: List<WebIllust>? = null
) : Serializable

class IllustHorizontalHolder(val data: OneLine) : SlinkyItem() {
    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return data.title == (other as? IllustHorizontalHolder)?.data?.title
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return data.title == (other as? IllustHorizontalHolder)?.data?.title
    }
}


@ItemHolder(IllustHorizontalHolder::class)
class IllustHorizontalViewHolder(aa: ItemIllustHorizontalListBinding) :
    SlinkyViewHolder<ItemIllustHorizontalListBinding, IllustHorizontalHolder>(aa) {

    override fun onBindViewHolder(item: IllustHorizontalHolder) {
        super.onBindViewHolder(item)

        binding.title.text = item.data.title
        binding.title.setOnClick {
            if (item.data.title?.isNotEmpty() == true) {
                it.findFragment<NavFragment>().searchTagImpl(item.data.title, false)
            }
        }

        if (item.data.displayIllust?.isNotEmpty() == true) {
            binding.listView.setUpGridlayoutManager(context, 3)
            val adapter = SLAdapter(lifecycleOwner)
            binding.listView.adapter = adapter
            adapter.submitList(item.data.displayIllust.map { illust ->
                IllustSquareHolder(illust)
            })
        }
    }
}

class IllustSquareHolder(val illust: WebIllust) : SlinkyItem() {
    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return illust.id == (other as? IllustSquareHolder)?.illust?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illust == (other as? IllustSquareHolder)?.illust
    }
}


@ItemHolder(IllustSquareHolder::class)
class IllustSquareViewHolder(aa: ItemIllustSquareBinding) :
    SlinkyViewHolder<ItemIllustSquareBinding, IllustSquareHolder>(aa) {

    override fun onBindViewHolder(item: IllustSquareHolder) {
        super.onBindViewHolder(item)
        val radius = context.dipToPx(6F)
        Glide.with(context)
            .load(item.illust.url?.toGlideUrl())
            .transform(RoundedCorners(radius))
            .into(binding.imageView)
        binding.imageView.setOnClick {
            it.findActionReceiverOrNull<WebIllustAction>()?.onClickWebIllust(item.illust)
        }
    }
}