package ceui.lisa.slinky.ui

import android.util.Size
import androidx.lifecycle.LiveData
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.CellItemIllustSeriesBinding
import ceui.lisa.slinky.databinding.ItemIllustHeaderHolderBinding
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.IllustSeries
import ceui.lisa.slinky.network.ObjectPool
import java.io.File

class IllustHeaderHolder(val illust: Illust) : SlinkyItem()


@ItemHolder(IllustHeaderHolder::class)
class IllustHeaderViewHolder(aa: ItemIllustHeaderHolderBinding) :
    SlinkyViewHolder<ItemIllustHeaderHolderBinding, IllustHeaderHolder>(aa) {

    override fun onBindViewHolder(item: IllustHeaderHolder) {
        super.onBindViewHolder(item)
    }
}

data class SizedImageFile(val file: File, val size: Size)


class IllustSeriesHolder(val illustSeries: LiveData<IllustSeries>) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return illustSeries.value?.illust_series_detail?.id == (other as? IllustSeriesHolder)?.illustSeries?.value?.illust_series_detail?.id
    }


    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illustSeries.value?.illust_series_detail == (other as? IllustSeriesHolder)?.illustSeries?.value?.illust_series_detail
    }
}


@ItemHolder(IllustSeriesHolder::class)
class IllustSeriesViewHolder(aa: CellItemIllustSeriesBinding) :
    SlinkyViewHolder<CellItemIllustSeriesBinding, IllustSeriesHolder>(aa) {

    override fun onBindViewHolder(item: IllustSeriesHolder) {
        super.onBindViewHolder(item)
        item.illustSeries.observe(lifecycleOwner) { series ->
            series.illust_series_context?.prev?.let { illust: Illust ->
                ObjectPool.updateIllust(illust)
                binding.startButton.text = illust.title
                binding.startButton.setOnClick {
                    it.findFragmentOrNull<NavFragment>()?.pushFragment(R.id.illustFragment, IllustFragmentArgs(illust.id).toBundle())
                }
            }
            series.illust_series_context?.next?.let { illust: Illust ->
                ObjectPool.updateIllust(illust)
                binding.endButton.text = illust.title
                binding.endButton.setOnClick {
                    it.findFragmentOrNull<NavFragment>()?.pushFragment(R.id.illustFragment, IllustFragmentArgs(illust.id).toBundle())
                }
            }
        }
    }
}


