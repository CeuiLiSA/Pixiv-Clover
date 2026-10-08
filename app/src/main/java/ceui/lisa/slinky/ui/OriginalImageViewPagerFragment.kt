package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import ceui.lisa.slinky.ActionItem
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentImageViewpagerBinding
import ceui.lisa.slinky.glide.saveImageImpl
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.ui.background.applyIllustForBackground
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import kotlinx.coroutines.delay

class OriginalImageViewPagerFragment : NavFragment(R.layout.fragment_image_viewpager) {

    private val safeArgs: OriginalImageViewPagerFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentImageViewpagerBinding::bind)
    private val imageViewModel: ImageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewModel.shouldHideStatusBar.observe(viewLifecycleOwner) { show ->
            binding.save.isVisible = show
            binding.toolbarContainer.isVisible = show
        }

        ObjectPool.get<Illust>(safeArgs.illustId).observe(viewLifecycleOwner) { illust ->
            val pageCount = illust.page_count

            actionbarContent.endItems.value = listOf(ActionItem(R.drawable.ic_more) {
                launchSuspend {
                    if (alertYesOrCancel(message = getString(R.string.apply_this_image_for_background))) {
                        val originalUrl = findOriginalUrl(illust, binding.viewPager.currentItem)
                        applyIllustForBackground(originalUrl, safeArgs.illustId)
                    }
                }
            })

            val tv = binding.save
            if (pageCount > 1) {
                binding.viewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        val humanIndex = (position + 1)
                        val str =
                            getString(R.string.save_origin_image_with_index, humanIndex, pageCount)
                        tv.post {
                            tv.text = str
                        }
                    }
                })
            } else {
                val str = getString(R.string.save_origin_image)
                tv.post {
                    tv.text = str
                }
            }

            binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                override fun getItemCount(): Int {
                    return pageCount
                }

                override fun createFragment(position: Int): Fragment {
                    val originalUrl = findOriginalUrl(illust, position)
                    return ImageFragment().apply {
                        if (pageCount > 1) {
                            if (position == 0) {
                                arguments = ImageFragmentArgs(
                                    url = originalUrl,
                                    lowQualityUrl = illust.image_urls?.large,
                                    illustId = safeArgs.illustId
                                ).toBundle()
                            } else {
                                arguments = ImageFragmentArgs(
                                    url = originalUrl,
                                    lowQualityUrl = null,
                                    illustId = safeArgs.illustId
                                ).toBundle()
                            }
                        } else {
                            arguments = ImageFragmentArgs(
                                url = originalUrl,
                                lowQualityUrl = illust.image_urls?.large,
                                illustId = safeArgs.illustId
                            ).toBundle()
                        }
                    }
                }
            }
            binding.viewPager.setCurrentItem(safeArgs.index, false)

            binding.save.setOnClick {
                launchSuspend {
                    it.showProgress()
                    delay(200L)
                    saveImageImpl(safeArgs.illustId, binding.viewPager.currentItem)
                    it.hideProgress()
                }
            }
        }
    }

    private fun findOriginalUrl(illust: Illust, position: Int): String {
        val pageCount = illust.page_count
        return if (pageCount > 1) {
            illust.meta_pages?.get(position)?.image_urls?.original ?: ""
        } else {
            illust.meta_single_page?.original_image_url ?: ""
        }
    }
}