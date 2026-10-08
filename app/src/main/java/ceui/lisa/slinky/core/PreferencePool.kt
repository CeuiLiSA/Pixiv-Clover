package ceui.lisa.slinky.core

import ceui.lisa.slinky.models.AccountResponse
import ceui.lisa.slinky.models.ModelObject
import ceui.lisa.slinky.models.PreferenceObject
import ceui.lisa.slinky.network.LiveDataPool
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.network.requireGlobalPref
import ceui.lisa.slinky.network.requireUserPrefImpl
import ceui.lisa.slinky.ui.settings.LOCAL_SETTING_KEY
import ceui.lisa.slinky.ui.settings.LocalSetting
import com.tencent.mmkv.MMKV
import timber.log.Timber


object PreferencePool : LiveDataPool() {

    private val userPrefStore by lazy { requireUserPrefImpl() }
    private val globalPrefStore by lazy { requireGlobalPref() }

    fun <ObjectT : PreferenceObject> updatePref(obj: ObjectT) {
        val json = Util.gson.toJson(obj)
        userPrefStore.putString(obj.prefKey, json)
        update(obj)
    }


    fun load() {
        typedKeyList.forEach { typedKey ->
            val prefStore: MMKV = if (typedKey.isGlobal) {
                globalPrefStore
            } else {
                userPrefStore
            }
            val json = prefStore.getString(typedKey.prefKey, typedKey.defaultValue)

            if (json?.isNotEmpty() == true) {
                try {
                    val obj = Util.gson.fromJson(json, typedKey.classSpec)
                    if (obj is ModelObject) {
                        update(obj)
                        if (obj is AccountResponse) {
                            obj.user?.let {
                                ObjectPool.update(it)
                            }
                        }
                        Timber.d("PreferencePool update $obj")
                    }
                } catch (ex: Exception) {
                    Timber.e(ex)
                }
            }
        }
    }


    private val typedKeyList = listOf(
        TypedKey(LocalSetting::class.java, LOCAL_SETTING_KEY, false, ""),
        TypedKey(AccountResponse::class.java, Settings.USER_TAG, true, Settings.UN_LOGIN),
    )

    private data class TypedKey(
        val classSpec: Class<*>,
        val prefKey: String,
        val isGlobal: Boolean,
        val defaultValue: String
    )
}