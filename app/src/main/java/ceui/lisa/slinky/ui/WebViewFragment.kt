package ceui.lisa.slinky.ui

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentWebViewBinding
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.requireUserPrefImpl
import ceui.lisa.slinky.styles.ProgressTextButton
import ceui.lisa.slinky.ui.dialog.alertNotice
import ceui.lisa.slinky.ui.dialog.showSpinner
import com.blankj.utilcode.util.BarUtils
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import timber.log.Timber


class WebViewFragment : CacheViewFragment(R.layout.fragment_web_view) {

    private val binding by viewBinding(FragmentWebViewBinding::bind)
    private val safeArgs: WebViewFragmentArgs by navArgs()

    override fun onViewCreated() {
        super.onViewCreated()
        if (safeArgs.url.isEmpty()) {
            return
        }

        binding.colorHeader.updateLayoutParams {
            height = BarUtils.getStatusBarHeight()
        }

        val webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url
                if (url != null) {
                    val urlString = url.toString()
                    Timber.d("shouldOverrideUrlLoading ${url.path}, ${url.host}, ${url.port}, ${url.query}, ${url.queryParameterNames}")
                    val path = url.path ?: ""
                    if (urlString.startsWith(PIXIV_URL_HEAD) && urlString.contains(url_artworks)) {
                        if (path.contains("/")) {
                            return try {
                                val illustId = path.split("/").last().toLong()
                                showIllust(illustId)
                                true
                            } catch (ex: Exception) {
                                Timber.e(ex)
                                false
                            }
                        }
                    } else if (urlString.startsWith(PIXIV_URL_HEAD) && urlString.contains(url_users)) {
                        if (path.contains("/")) {
                            return try {
                                val userId = path.split("/").last().toLong()
                                showUser(userId)
                                true
                            } catch (ex: Exception) {
                                Timber.e(ex)
                                false
                            }
                        }
                    }
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        val webChromeClient = object : WebChromeClient() {
        }

        val instance = AgentWeb.with(this)
            .setAgentWebParent(binding.webViewHolder, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .setWebChromeClient(webChromeClient)
            .setWebViewClient(webViewClient)
            .createAgentWeb()
            .go(safeArgs.url)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val webView = instance.webCreator.webView ?: return
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        performBack()
                    }
                }
            })
    }
}

fun NavFragment.showIllust(illustId: Long, sender: ProgressTextButton? = null) {
    launchSuspend {
        if (sender == null) {
            val spinner = showSpinner()
            try {
                val illustResponse = Client.appApi.getIllustById(illustId)
                if (illustResponse.illust != null) {
                    ObjectPool.updateIllust(illustResponse.illust)
                    onClickIllustImpl(illustResponse.illust)
                } else {
                    alertNotice(message = getString(R.string.illust_not_found))
                }
            } catch (ex: Exception) {
                handleError(ex)
            } finally {
                spinner.end()
            }
        } else {
            try {
                sender.showProgress()
                val illustResponse = Client.appApi.getIllustById(illustId)
                if (illustResponse.illust != null) {
                    ObjectPool.updateIllust(illustResponse.illust)
                    onClickIllustImpl(illustResponse.illust)
                } else {
                    alertNotice(message = getString(R.string.illust_not_found))
                }
            } catch (ex: Exception) {
                handleError(ex)
            } finally {
                sender.hideProgress()
            }
        }
    }
}

fun NavFragment.showUser(userId: Long) {
    launchSuspend {
        val spinner = showSpinner()
        try {
            val userResponse = Client.appApi.user(userId)
            if (userResponse.user != null) {
                ObjectPool.update(userResponse.user)
                pushFragment(R.id.userFragment, UserFragmentArgs(userId).toBundle())
            } else {
                alertNotice(message = getString(R.string.illust_not_found))
            }
        } catch (ex: Exception) {
            handleError(ex)
        } finally {
            spinner.end()
        }
    }
}