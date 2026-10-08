package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.packageInfo
import ceui.lisa.slinky.showPush
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import ceui.lisa.slinky.ui.settings.requireLocalSetting
import ceui.lisa.slinky.ui.settings.updateLocalSetting
import ceui.lisa.slinky.utils.openWebPageWithSystemBrowser
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.scwang.smart.refresh.header.FalsifyHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsRepository : CustomRepository<SettingsFragment>() {

    private val imageCacheLiveData = MutableLiveData(0L)
    private val gifCacheLiveData = MutableLiveData(0L)

    override fun attachFragment(fragment: SettingsFragment) {
        super.attachFragment(fragment)
        coroutineScope.launch {
            async {
                calculateImageCacheSizeImpl()
            }
            async {
                calculateGifCacheSizeImpl()
            }
        }
    }

    override suspend fun suspendRefresh(
        fragment: SettingsFragment
    ) {
        with(fragment) {
            val tagItems = mutableListOf<SlinkyItem>()
            val localSetting = requireLocalSetting()
            tagItems.add(TabSectionHolder(getString(R.string.general)))
            tagItems.add(
                TabToggleHolder(
                    getString(R.string.download_successfully_sound),
                    localSetting.downloadSuccessfullySound
                ) { isChecked ->
                    val newlySetting = requireLocalSetting()
                    updateLocalSetting(newlySetting.copy(downloadSuccessfullySound = isChecked))
                })
            tagItems.add(TabHolder(getString(R.string.app_background_image)) {
                pushFragment(R.id.backgroundFragment)
            })
            tagItems.add(TabHolder(getString(R.string.app_background_image_blur)) {
                pushFragment(R.id.navigation_blur_background_fragment)
            })
            tagItems.add(TabHolder(getString(R.string.ai_works_display_settings)) {
                pushFragment(R.id.aiSettingsFragment)
            })
            tagItems.add(TabHolder(getString(R.string.export_user_json)) {
                val account = Settings.loggedInAccount
                val json = Util.gson.toJson(account)
                TextUtil.copyToPB(requireContext(), json)
                showPush(body = getString(R.string.copied))
            })
            tagItems.add(
                TabHolder(
                    getString(R.string.clear_img_cache),
                    imageCacheLiveData.map { length ->
                        FileSize.getFileSize(length)
                    }) {
                    launchSuspend {
                        if (alertYesOrCancel(message = getString(R.string.clear_img_cache))) {
                            clearImageCache(fragment)
                        }
                    }
                })
            tagItems.add(
                TabHolder(
                    getString(R.string.clear_gif_cache),
                    gifCacheLiveData.map { length ->
                        FileSize.getFileSize(length)
                    }) {
                    launchSuspend {
                        if (alertYesOrCancel(message = getString(R.string.clear_gif_cache))) {
                            clearGifCache(fragment)
                        }
                    }
                })
            tagItems.add(TabSectionHolder(getString(R.string.pixiv)))
            tagItems.add(TabHolder(getString(R.string.help)) {
                openWebPageWithSystemBrowser(SettingsFragment.HELP_URL_WEB)
            })
            tagItems.add(TabHolder(getString(R.string.terms_of_service)) {
                openWebPageWithSystemBrowser(SettingsFragment.Service_Master)
            })
            tagItems.add(TabHolder(getString(R.string.privacy_policy)) {
                openWebPageWithSystemBrowser(SettingsFragment.Privacy_Policy)
            })
            tagItems.add(TabHolder(getString(R.string.specific_exchange)) {
                openWebPageWithSystemBrowser(SettingsFragment.Notation)
            })
            tagItems.add(TabHolder(getString(R.string.payment_method)) {
                openWebPageWithSystemBrowser(SettingsFragment.Shikin)
            })
            tagItems.add(TabHolder(getString(R.string.site_policy)) {
                openWebPageWithSystemBrowser(SettingsFragment.PAGE_GUIDE_LINE)
            })
            val packageInfo = packageInfo()
            val versionText =
                getString(R.string.version, "${packageInfo.versionName}-${packageInfo.versionCode}")
            tagItems.add(AppVersionHolder(versionText))
            holderList.value = tagItems
            refreshState.value = LoadState.LOADED(hasContent = true, hasNext = false)
        }
    }

    private suspend fun calculateImageCacheSizeImpl() {
        withContext(Dispatchers.IO) {
            var imageCacheLength = 0L
            val imageCacheDir =
                File(PathUtils.getInternalAppCachePath() + "/image_manager_disk_cache")
            if (imageCacheDir.exists() && imageCacheDir.isDirectory) {
                imageCacheDir.listFiles()?.forEach { imgFile ->
                    imageCacheLength += imgFile.length()
                }
            }
            withContext(Dispatchers.Main) {
                imageCacheLiveData.value = imageCacheLength
            }
        }
    }

    private suspend fun calculateGifCacheSizeImpl() {
        withContext(Dispatchers.IO) {
            var gifCacheLength = 0L
            val gifCacheDir = File(PathUtils.getInternalAppCachePath() + "/gifUnzipFiles")
            if (gifCacheDir.exists() && gifCacheDir.isDirectory) {
                gifCacheDir.listFiles()?.forEach { gifUnzipFolder ->
                    if (gifUnzipFolder.exists() && gifUnzipFolder.exists()) {
                        gifUnzipFolder.listFiles()?.forEach { imgFile ->
                            gifCacheLength += imgFile.length()
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                gifCacheLiveData.value = gifCacheLength
            }
        }
    }

    private fun clearImageCache(fragment: SettingsFragment) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val imageCacheDir =
                    File(PathUtils.getInternalAppCachePath() + "/image_manager_disk_cache")
                if (imageCacheDir.exists() && imageCacheDir.isDirectory) {
                    val result = FileUtils.deleteAllInDir(imageCacheDir)
                    if (result) {
                        withContext(Dispatchers.Main) {
                            fragment.showPush(body = fragment.getString(R.string.clear_successfully))
                        }
                    }
                }
                calculateImageCacheSizeImpl()
            }
        }
    }

    private suspend fun clearGifCache(fragment: SettingsFragment) {
        withContext(Dispatchers.IO) {
            val gifCacheDir = File(PathUtils.getInternalAppCachePath() + "/gifUnzipFiles")
            if (gifCacheDir.exists() && gifCacheDir.isDirectory) {
                val result = FileUtils.deleteAllInDir(gifCacheDir)
                if (result) {
                    withContext(Dispatchers.Main) {
                        fragment.showPush(body = fragment.getString(R.string.clear_successfully))
                    }
                }
            }
            calculateGifCacheSizeImpl()
        }
    }
}

class SettingsFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel { SettingsRepository() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refreshLayout.setRefreshHeader(FalsifyHeader(requireContext()))
        actionbarContent.title.value = getString(R.string.settings)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }

    companion object {
        const val HELP_URL = "https://app.pixiv.help/hc/zh-cn"
        const val HELP_URL_WEB =
            "https://www.pixiv.help/hc/zh-cn?utm_campaign=footer&utm_medium=help_link&utm_source=www_pixiv"
        const val PAGE_GUIDE_LINE = "https://www.pixiv.net/terms/?page=guideline"
        const val Service_Master = "https://policies.pixiv.net/en.html"
        const val Privacy_Policy = "https://policies.pixiv.net/en.html#privacy"
        const val Shikin = "https://policies.pixiv.net/en.html#shikin"
        const val Notation = "https://policies.pixiv.net/en.html#notation"
    }
}

