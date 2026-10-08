package ceui.lisa.slinky.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import ceui.lisa.slinky.R
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.WebViewFragmentArgs
import com.bumptech.glide.load.model.GlideUrl
import java.util.Locale

fun Context.openWebPageWithSystemBrowser(urlString: String) {
    val parsedUrl = Uri.parse(urlString)
    val url = parsedUrl.buildUpon().scheme(parsedUrl.scheme?.lowercase(Locale.ROOT)).build()
    try {
        // open a chrome tab
        val customTabsIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(getColor(R.color.page_default_background)).build()
            ).setStartAnimations(this, R.anim.h_slide_enter, R.anim.h_slide_exit)
            .build()
        customTabsIntent.launchUrl(this, url)
    } catch (ex1: Exception) {
        ex1.printStackTrace()
        try {
            // browser picker
            val intent = Intent(Intent.ACTION_VIEW, url)
            startActivity(intent)
        } catch (ex2: Exception) {
            ex2.printStackTrace()
        }
    }
}

fun NavFragment.openWebPageWithSystemBrowser(url: String) {
    requireContext().openWebPageWithSystemBrowser(url)
}

fun NavFragment.openWebPageInApp(url: String) {
    pushFragment(R.id.webViewFragment, WebViewFragmentArgs(url).toBundle())
}

const val MAP_KEY_SMALL = "referer"
const val IMAGE_REFERER = "https://app-api.pixiv.net/"

fun String.toGlideUrl(): GlideUrl {
    val hashMap = HashMap<String, String>()
    hashMap[MAP_KEY_SMALL] = IMAGE_REFERER
    return GlideUrl(this) { hashMap }
}