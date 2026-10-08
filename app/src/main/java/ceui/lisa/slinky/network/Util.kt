package ceui.lisa.slinky.network

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

object Util {

    val pkceItem: PKCEItem by lazy {
        buildPKCEItem()
    }
    val gson: Gson by lazy {
        Gson()
    }
    val timeFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    }

    private fun buildPKCEItem(): PKCEItem {
        val verify = PkceUtil.generateCodeVerifier()
        val challenge = PkceUtil.generateCodeChallange(verify)
        return PKCEItem(verify, challenge)
    }
}