package ceui.lisa.slinky.network

import ceui.lisa.slinky.requireLoggedInAccountImpl
import ceui.lisa.slinky.ui.NavFragment
import com.tencent.mmkv.MMKV

fun requireUserPrefImpl(): MMKV {
    val loggedInUser = requireLoggedInAccountImpl().user
    return if (loggedInUser?.id != null && loggedInUser.id != -1L) {
        MMKV.mmkvWithID(loggedInUser.id.toString())
    } else {
        requireGlobalPref()
    }
}


fun requireGlobalPref(): MMKV {
    return MMKV.defaultMMKV()
}