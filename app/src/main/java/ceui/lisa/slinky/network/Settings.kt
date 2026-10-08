package ceui.lisa.slinky.network

import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.models.AccountResponse
import ceui.lisa.slinky.ui.addRecentLoggedInUser
import ceui.lisa.slinky.ui.settings.LOCAL_SETTING_KEY
import ceui.lisa.slinky.ui.settings.LocalSetting
import com.blankj.utilcode.util.AppUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object Settings {

    const val USER_TAG = "SLINKY_LOGGED_IN_USER"
    const val UN_LOGIN = "{\"user\":{\"id\":\"-1\"}}"

    val settingsInstance = MutableLiveData<LocalSetting>()
    var loggedInAccount: AccountResponse = buildLoggedInUser()

    fun loadSettings() {
        val pref = requireUserPrefImpl()
        val storedLocalSettingString = pref.getString(LOCAL_SETTING_KEY, "")
        val setting = if (storedLocalSettingString?.isNotEmpty() == true) {
            Util.gson.fromJson(storedLocalSettingString, LocalSetting::class.java)
        } else {
            defaultSettings()
        }
        settingsInstance.value = setting
    }

    fun saveSettings() {
        val pref = requireUserPrefImpl()
        settingsInstance.value?.let {
            pref.putString(LOCAL_SETTING_KEY, Util.gson.toJson(it))
        }
    }

    private fun defaultSettings(): LocalSetting {
        val pref = requireUserPrefImpl()
        val setting = LocalSetting()
        pref.putString(LOCAL_SETTING_KEY, Util.gson.toJson(setting))
        return setting
    }

    private fun buildLoggedInUser(): AccountResponse {
        val userGson = requireGlobalPref().getString(USER_TAG, UN_LOGIN)
        val accountResponse = Util.gson.fromJson(userGson, AccountResponse::class.java)
        accountResponse.user?.let {
            ObjectPool.update(it)
        }
        return accountResponse
    }

    fun updateLoggedInUser(accountResponse: AccountResponse) {
        val userGson = Util.gson.toJson(accountResponse)
        requireGlobalPref().putString(USER_TAG, userGson)
        accountResponse.user?.let {
            ObjectPool.update(it)
        }
        loggedInAccount = accountResponse
        addRecentLoggedInUser(accountResponse)
    }

    fun logOut() {
        MainScope().launch {
            requireGlobalPref().putString(USER_TAG, UN_LOGIN)
            loggedInAccount = buildLoggedInUser()
            AppUtils.relaunchApp()
        }
    }
}