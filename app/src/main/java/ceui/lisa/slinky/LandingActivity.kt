package ceui.lisa.slinky

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import ceui.lisa.slinky.databinding.ActivityLandingBinding
import ceui.lisa.slinky.models.AccountResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.SettingsFragment
import ceui.lisa.slinky.ui.dialog.alertNotice
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.ui.showIllust
import ceui.lisa.slinky.ui.showUser
import ceui.lisa.slinky.utils.openWebPageWithSystemBrowser
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.zackratos.ultimatebarx.ultimatebarx.navigationBar
import com.zackratos.ultimatebarx.ultimatebarx.statusBar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun SpannableString.setLinkSpan(
    text: String,
    hideUnderLine: Boolean = true,
    color: Int? = null,
    action: () -> Unit
) {
    val textIndex = this.indexOf(text)
    if (textIndex >= 0) {
        setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    action()
                }

                override fun updateDrawState(ds: TextPaint) {
                    color?.let {
                        ds.linkColor = it
                    }
                    if (hideUnderLine) {
                        ds.color = ds.linkColor
                        ds.isUnderlineText = false
                    } else {
                        super.updateDrawState(ds)
                    }
                }
            },
            textIndex,
            textIndex + text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

class LandingViewModel : ViewModel() {

    val isChecked = MutableLiveData(false)
}

class LandingActivity : FullScreenActivity() {

    private val viewModel: LandingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding =
            DataBindingUtil.setContentView<ActivityLandingBinding>(this, R.layout.activity_landing)

        binding.firstText.movementMethod = LinkMovementMethod.getInstance()
        val matchTOS = getString(R.string.terms_of_service)
        val matchPP = getString(R.string.privacy_policy)
        val terms = String.format(getString(R.string.landing_terms_base), matchTOS, matchPP)
        binding.firstText.text = SpannableString(terms).apply {
            this.setLinkSpan(matchTOS, hideUnderLine = false) {
                openWebPageWithSystemBrowser(SettingsFragment.Service_Master)
            }
            this.setLinkSpan(matchPP, hideUnderLine = false) {
                openWebPageWithSystemBrowser(SettingsFragment.Privacy_Policy)
            }
        }

        viewModel.isChecked.observe(this) {
            binding.checkboxOne.isSelected = it
        }

        binding.checkboxOne.setOnClick {
            val res = viewModel.isChecked.value ?: false
            viewModel.isChecked.value = !res
        }

        val logInButton = binding.logIn
        logInButton.setOnClick {
            checkAndNext {
                openWebPageWithSystemBrowser(LOGIN_HEAD + Util.pkceItem.challenge + LOGIN_END)
            }
        }
        val signUpButton = binding.signUp
        signUpButton.setOnClick {
            checkAndNext {
                openWebPageWithSystemBrowser(SIGN_HEAD + Util.pkceItem.challenge + SIGN_END)
            }
        }

        binding.cloverLogo.setOnClick {
            val userJson = ClipboardUtils.getText()?.toString()
            if (userJson?.isNotEmpty() == true) {
                safeCall {
                    val account = Util.gson.fromJson(userJson, AccountResponse::class.java)
                    Settings.updateLoggedInUser(account)
                    AppUtils.relaunchApp()
                }
            }
        }
        binding.cloverLogo.startAnimation(AnimationSet(true).also {
            it.interpolator = AccelerateDecelerateInterpolator()
            it.addAnimation(TranslateAnimation(0f, 0f, -20f, 20f).apply {
                duration = 5000L
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
                interpolator = LinearInterpolator()
            })
            it.addAnimation(
                RotateAnimation(
                    (-8..-3).random().toFloat(),
                    (3..8).random().toFloat(),
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                ).apply {
                    duration = 3000L
                    repeatMode = Animation.REVERSE
                    repeatCount = Animation.INFINITE
                    interpolator = LinearInterpolator()
                })
        })

        val packageInfo = packageInfo()
        binding.versionText.text =
            getString(R.string.version, "${packageInfo.versionName}-${packageInfo.versionCode}")
    }

    private fun checkAndNext(block: () -> Unit) {
        if (viewModel.isChecked.value == true) {
            block()
        } else {
            lifecycleScope.launch {
                alertNotice(title = null, message = getString(R.string.read_agreement))
            }
        }
    }

    companion object {

        private const val LOGIN_HEAD = "https://app-api.pixiv.net/web/v1/login?code_challenge="
        private const val LOGIN_END = "&code_challenge_method=S256&client=pixiv-ios"

        private const val SIGN_HEAD =
            "https://app-api.pixiv.net/web/v1/provisional-accounts/create?code_challenge="
        private const val SIGN_END = "&code_challenge_method=S256&client=pixiv-ios"
    }
}

fun Context.packageInfo(): PackageInfo {
    return packageManager.getPackageInfo(packageName, 0)
}

fun NavFragment.packageInfo(): PackageInfo {
    return requireContext().packageInfo()
}

object TaskQueue {

    var shouldBeInvoked: ((NavFragment) -> Unit)? = null
}

class OutWakeActivity : FullScreenActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //pixiv://users/38674
        if (intent != null && intent.data != null) {
            if (intent.data?.scheme?.contains("pixiv") == true) {
                if (TextUtils.equals(intent.data?.host, "illusts")) {
                    val path = intent.data?.path
                    val illustId = path?.substring(1)
                    if (illustId != null) {
                        TaskQueue.shouldBeInvoked = { fragment ->
                            fragment.showIllust(illustId.toLong())
                        }
                        finish()
                        return
                    }
                } else if (TextUtils.equals(intent.data?.host, "users")) {
                    val path = intent.data?.path
                    val userId = path?.substring(1)
                    if (userId != null) {
                        TaskQueue.shouldBeInvoked = { fragment ->
                            fragment.showUser(userId.toLong())
                        }
                        finish()
                        return
                    }
                }

                val codeFromIntent = intent.data?.getQueryParameter("code") ?: return
                MainScope().launch {
                    try {
                        val account = Client.authApi.logIn(
                            code_verifier = Util.pkceItem.verify,
                            code = codeFromIntent
                        )
                        Settings.updateLoggedInUser(account)
                        AppUtils.relaunchApp()
                    } catch (ex: Exception) {
                        handleError(ex)
                    }
                }
            }
        }
    }
}

class TouchScreenViewModel : ViewModel() {

    var shouldAdjustKeyboard = false
}

open class FullScreenActivity : AppCompatActivity() {

    private val viewModel: TouchScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )
        super.onCreate(savedInstanceState)
//        WindowCompat.setDecorFitsSystemWindows(window, false)

        statusBar { transparent() }
        navigationBar { transparent() }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (viewModel.shouldAdjustKeyboard && ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideKeyboard(v, ev)) {
                KeyboardUtils.hideSoftInput(this)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if (v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationOnScreen(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            return !(event.rawX > left && event.rawY > top && event.rawY < bottom)
        }
        return false
    }

    fun updateShouldAdjustKeyboard(should: Boolean) {
        viewModel.shouldAdjustKeyboard = should
    }
}
