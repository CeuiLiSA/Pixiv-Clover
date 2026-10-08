package ceui.lisa.slinky.ui.background

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.ActionItem
import ceui.lisa.slinky.MainActivity
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.CellSeekbarBinding
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.ui.SelectionHolder
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import ceui.lisa.slinky.ui.performBack
import timber.log.Timber
import kotlin.math.roundToInt


private class Repository : CustomRepository<BackgroundBlurSettingsFragment>() {

    private val blurRadius = MutableLiveData(4F)
    private val isBlurEnabled = MutableLiveData(false)

    override fun attachFragment(
        fragment: BackgroundBlurSettingsFragment
    ) {
        run {
            val settings = Settings.settingsInstance.value
            blurRadius.value = settings?.backgroundBlurRadius ?: 4F
            isBlurEnabled.value = settings?.backgroundBlurEnabled ?: false
        }

        fragment.actionbarContent.endItems.value = listOf(
            ActionItem(R.drawable.ic_done) {
                Settings.saveSettings()
                fragment.performBack()
            }
        )

        blurRadius.observe(fragment.viewLifecycleOwner) {
            val settings = Settings.settingsInstance.value
            Settings.settingsInstance.value = settings?.copy(backgroundBlurRadius = it)
        }
    }

    override suspend fun suspendRefresh(
        fragment: BackgroundBlurSettingsFragment
    ) {
        with(fragment) {
            holderList.value = buildList {
                add(
                    SelectionHolder(
                        getString(R.string.no_need_to_blur),
                        isBlurEnabled.map { it != true }
                    ).bindOnClick {
                        isBlurEnabled.value = false
                        val settings = Settings.settingsInstance.value
                        Settings.settingsInstance.value = settings?.copy(backgroundBlurEnabled = false)
                        Settings.saveSettings()
                        dispatchRefresh(fragment, RefreshHint.pullToRefresh())
                    }
                )
                add(
                    SelectionHolder(
                        getString(R.string.need_to_blur),
                        isBlurEnabled.map { it == true }
                    ).bindOnClick {
                        isBlurEnabled.value = true
                        val settings = Settings.settingsInstance.value
                        Settings.settingsInstance.value = settings?.copy(backgroundBlurEnabled = true)
                        Settings.saveSettings()
                        dispatchRefresh(fragment, RefreshHint.pullToRefresh())
                    }
                )
                if (isBlurEnabled.value == true) {
                    add(SeekBarHolder(blurRadius))
                }
            }
            refreshState.value = LoadState.LOADED(hasContent = true, hasNext = false)
        }
    }
}

class BackgroundBlurSettingsFragment : SlinkyListFragment() {

    private val listViewModel by listViewModel { Repository() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSlinkyListBinding.bind(view)
        actionbarContent.title.value = getString(R.string.app_background_image_blur)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, listViewModel)
    }
}

class SeekBarHolder(val radius: MutableLiveData<Float>) : SlinkyItem() {


}


@ItemHolder(SeekBarHolder::class)
class SeekBarViewHolder(val aa: CellSeekbarBinding) : SlinkyViewHolder<CellSeekbarBinding, SeekBarHolder>(aa) {

    override fun onBindViewHolder(item: SeekBarHolder) {
        super.onBindViewHolder(item)
        binding.holder = item
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.radiusSeekBar.min = 100
        }
        binding.radiusSeekBar.max = 4000
        item.radius.observe(lifecycleOwner) {
            val v = (it * 100F).roundToInt()
            if (binding.radiusSeekBar.progress != v) {
                binding.radiusSeekBar.progress = v
            }
        }
        binding.radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    item.radius.value = progress / 100F
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
}