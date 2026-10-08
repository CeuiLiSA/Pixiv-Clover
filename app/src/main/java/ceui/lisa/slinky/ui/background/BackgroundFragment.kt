package ceui.lisa.slinky.ui.background

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.core.waitForValue
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.showPush
import ceui.lisa.slinky.ui.BookmarkType
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SubmittingDialog
import ceui.lisa.slinky.ui.dialog.alertNotice
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import ceui.lisa.slinky.ui.exist
import ceui.lisa.slinky.ui.launchSuspend
import ceui.lisa.slinky.ui.performBack
import ceui.lisa.slinky.ui.pxValue
import ceui.lisa.slinky.ui.setUpGridlayoutManager
import ceui.lisa.slinky.ui.settings.requireLocalSetting
import ceui.lisa.slinky.ui.settings.updateLocalSetting
import ceui.lisa.slinky.ui.task.DownloadTask
import ceui.lisa.slinky.ui.viewBinding
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.UriUtils
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.File
import java.util.UUID

class BackgroundRepository(
    private val senderId: Long,
    private val currentBackground: LiveData<AppBackground>
) : PixivListRepository<Illust, BackgroundFragment>(
    loader = { Client.appApi.userBookmarkedIllust(senderId, BookmarkType.PUBLIC) },
    dataMapper = { illust ->
        BackgroundHolder(
            illust,
            currentBackground,
            BackgroundType.CHOOSE_ILLUST
        )
    }
) {
    override suspend fun applyRefreshData(
        fragment: BackgroundFragment,
        displayList: List<Illust>
    ) {
        val headerItems = mutableListOf<SlinkyItem>(
            BackgroundHolder(
                null,
                currentBackground,
                BackgroundType.PURE_COLOR,
                AppBackground.DEFAULT_COLOR
            ),
            BackgroundHolder(null, currentBackground, BackgroundType.FILE_FROM_GALLERY)
        )
        val bg = currentBackground.waitForValue(fragment.viewLifecycleOwner)
        val backgroundIllustId = (bg as? IllustBackground)?.illustId
        if (backgroundIllustId.exist() && displayList.all { it.id != backgroundIllustId }) {
            requireNotNull(backgroundIllustId)
            val illustResponse = Client.appApi.getIllustById(backgroundIllustId)
            if (illustResponse.illust != null) {
                ObjectPool.updateIllust(illustResponse.illust)
                headerItems.add(
                    BackgroundHolder(
                        illustResponse.illust,
                        currentBackground,
                        BackgroundType.CHOOSE_ILLUST
                    )
                )
            }
        }
        holderList.value = headerItems + displayList.map(dataMapper)
    }
}

class BackgroundFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val currentBackground by lazy {
        Settings.settingsInstance.map { localSetting ->
            localSetting?.buildAppBackground() ?: PureColorBackground(AppBackground.DEFAULT_COLOR)
        }
    }
    private val viewModel by listViewModel(
        { senderId },
        { currentBackground }) { myselfId, background ->
        BackgroundRepository(myselfId, background)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.app_background_image)
        binding.listView.setUpGridlayoutManager(requireContext())
        binding.listView.updatePadding(left = 4.pxValue, right = 4.pxValue)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }

    fun didClickBackgroundHolder(sender: View, holder: BackgroundHolder) {
        if (holder.backgroundType == BackgroundType.CHOOSE_ILLUST && holder.illust != null) {
            val illust = holder.illust
            val imageUrl = if (illust.page_count == 1) {
                illust.meta_single_page?.original_image_url
            } else {
                illust.meta_pages?.getOrNull(0)?.image_urls?.original
            }
            applyIllustForBackground(imageUrl, illust.id)
        } else if (holder.backgroundType == BackgroundType.FILE_FROM_GALLERY) {
            pickImageFromGallery()
        } else if (holder.backgroundType == BackgroundType.PURE_COLOR) {
            setPureColorBackground()
        }
    }

    private fun setPureColorBackground() {
        val newlySetting = requireLocalSetting()
        if (newlySetting.backgroundType != BackgroundType.PURE_COLOR) {
            launchSuspend {
                if (alertYesOrCancel(message = getString(R.string.reset_bg_image_hint))) {
                    updateLocalSetting(
                        newlySetting.copy(
                            backgroundType = BackgroundType.PURE_COLOR,
                            backgroundColorString = AppBackground.DEFAULT_COLOR,
                            backgroundImageUri = null
                        )
                    )
                    showPush(title = getString(R.string.update_background_successfully))
                    delay(200L)
                    performBack()
                }
            }
        } else {
            launchSuspend {
                alertNotice(message = getString(R.string.already_default_bg_hint))
            }
        }
    }

    private val requestDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data ?: return@registerForActivityResult
                cropImage(imageUri) { outputUri ->
                    val localSetting = requireLocalSetting()
                    updateLocalSetting(
                        localSetting.copy(
                            backgroundType = BackgroundType.FILE_FROM_GALLERY,
                            backgroundImageUri = outputUri.toString()
                        )
                    )
                    showPush(title = getString(R.string.update_background_successfully))
                    delay(200L)
                    performBack()
                    Timber.tag("FileUriTest").d("outputUri: %s", outputUri)
                }
            }
        }

    private fun pickImageFromGallery() {
        val gallery = Intent(Intent.ACTION_PICK)
        gallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        requestDataLauncher.launch(gallery)
    }
}

fun NavFragment.applyIllustForBackground(imageUrl: String?, illustId: Long) {
    imageUrl?.let { url ->
        val task = DownloadTask(url)
        task.onComplete = { file ->
            cropImage(UriUtils.file2Uri(file)) { outputUri ->
                val localSetting = requireLocalSetting()
                updateLocalSetting(
                    localSetting.copy(
                        backgroundType = BackgroundType.CHOOSE_ILLUST,
                        backgroundIllustId = illustId,
                        backgroundImageUri = outputUri.toString()
                    )
                )
                showPush(title = getString(R.string.update_background_successfully))
                performBack()
            }
        }
        val dialog = SubmittingDialog(task)
        dialog.show(childFragmentManager, SubmittingDialog.TAG)
    }
}

fun NavFragment.cropImage(from: Uri, block: suspend (outputUri: Uri) -> Unit) {
    val uuid = UUID.randomUUID().toString()
    val parentFile = File(
        PathUtils.getInternalAppCachePath()
    )
    if (!parentFile.exists()) {
        parentFile.mkdir()
    }
    val destFile = File(parentFile, "slinky_background_${uuid}.png")
    if (!destFile.exists()) {
        destFile.createNewFile()
    }
    val destUri = destFile.toUri()
    val intent = UCrop.of(from, destUri).withAspectRatio(9F, 16F).getIntent(requireContext())
    Timber.d("cropImage from: ${from}, to: ${destUri}")
    cropFinishedBlock = block
    requestCropImage.launch(intent)
}