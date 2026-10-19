package ceui.lisa.slinky

import android.content.Intent
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import ceui.lisa.slinky.ui.WebViewFragment
import ceui.lisa.slinky.ui.WebViewFragmentArgs
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 26)
class WebViewSslErrorTest {

    @Test
    fun untrustedCertificateBlocksThePage() {
        val certificate = HeldCertificate.Builder()
            .commonName("localhost")
            .addSubjectAlternativeName("localhost")
            .build()
        val certificates = HandshakeCertificates.Builder()
            .heldCertificate(certificate)
            .build()

        MockWebServer().use { server ->
            server.useHttps(certificates.sslSocketFactory(), false)
            server.enqueue(MockResponse().setBody("This untrusted page must never load"))
            server.start()
            val pageUrl = server.url("/").toString()
            val sslErrorReceived = CountDownLatch(1)
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val monitor = ActivityLifecycleMonitorRegistry.getInstance()
            val activityRef = AtomicReference<LandingActivity>()

            // LandingActivity animates continuously, so do not wait for UI idleness.
            val callback = ActivityLifecycleCallback { activity, stage ->
                if (activity is LandingActivity && stage == Stage.RESUMED &&
                    activityRef.compareAndSet(null, activity)
                ) {
                    val container = FrameLayout(activity).apply { id = View.generateViewId() }
                    activity.setContentView(container)
                    activity.supportFragmentManager.beginTransaction()
                        .replace(container.id, WebViewFragment().apply {
                            arguments = WebViewFragmentArgs("about:blank").toBundle()
                        })
                        .commitNow()

                    val webView = requireNotNull(container.findWebView())
                    val productionClient = webView.webViewClient
                    webView.webViewClient = object : WebViewClient() {
                        override fun onReceivedSslError(
                            view: WebView,
                            handler: SslErrorHandler,
                            error: SslError
                        ) {
                            // Exercise the real AgentWeb client and the fragment's SSL handler.
                            productionClient.onReceivedSslError(view, handler, error)
                            sslErrorReceived.countDown()
                        }
                    }
                    webView.loadUrl(pageUrl)
                }
            }
            try {
                instrumentation.runOnMainSync {
                    monitor.addLifecycleCallback(callback)
                    instrumentation.targetContext.startActivity(
                        Intent(instrumentation.targetContext, LandingActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                assertTrue("WebView must report a certificate error", sslErrorReceived.await(15, TimeUnit.SECONDS))
                assertNull("A failed certificate must never allow an HTTP request", server.takeRequest(3, TimeUnit.SECONDS))
            } finally {
                instrumentation.runOnMainSync {
                    monitor.removeLifecycleCallback(callback)
                    activityRef.get()?.finish()
                }
            }
        }
    }

    private fun View.findWebView(): WebView? {
        if (this is WebView) return this
        if (this is ViewGroup) {
            for (index in 0 until childCount) {
                getChildAt(index).findWebView()?.let { return it }
            }
        }
        return null
    }
}
