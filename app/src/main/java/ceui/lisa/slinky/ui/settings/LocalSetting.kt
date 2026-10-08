package ceui.lisa.slinky.ui.settings

import android.content.Context
import ceui.lisa.slinky.models.PreferenceObject
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.network.requireUserPrefImpl
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.background.AppBackground
import ceui.lisa.slinky.ui.background.BackgroundType
import ceui.lisa.slinky.ui.background.FileFromGalleryBackground
import ceui.lisa.slinky.ui.background.IllustBackground
import ceui.lisa.slinky.ui.background.PureColorBackground
import java.io.Serializable

data class LocalSetting(
    val downloadSuccessfullySound: Boolean = true,
    val backgroundType: Int = 0,
    val backgroundBlurRadius: Float = 4F,
    val backgroundBlurEnabled: Boolean = false,
    val backgroundImageUri: String? = null,
    val backgroundIllustId: Long? = null,
    val backgroundColorString: String = AppBackground.DEFAULT_COLOR,
) : Serializable, PreferenceObject {

    fun buildAppBackground(): AppBackground {
        return if (backgroundType == BackgroundType.CHOOSE_ILLUST && backgroundIllustId != null && backgroundImageUri != null) {
            IllustBackground(backgroundIllustId, backgroundImageUri)
        } else if (backgroundType == BackgroundType.FILE_FROM_GALLERY && backgroundImageUri != null) {
            FileFromGalleryBackground(backgroundImageUri)
        } else {
            PureColorBackground(backgroundColorString)
        }
    }

    override val prefKey: String = LOCAL_SETTING_KEY
    override val objectUniqueId: Long = 998877665544L
}

fun NavFragment.requireLocalSetting(): LocalSetting {
    return requireContext().requireLocalSetting()
}

fun Context.requireLocalSetting(): LocalSetting {
    return Settings.settingsInstance.value ?: LocalSetting()
}

fun NavFragment.updateLocalSetting(setting: LocalSetting) {
    val pref = requireUserPrefImpl()
    pref.putString(LOCAL_SETTING_KEY, Util.gson.toJson(setting))
    Settings.settingsInstance.value = setting
}

const val LOCAL_SETTING_KEY = "LOCAL_SETTING_KEY"
