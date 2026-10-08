package ceui.lisa.slinky

import android.content.Context
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import ceui.lisa.slinky.databinding.ActivityMainBinding
import ceui.lisa.slinky.models.AccountResponse
import ceui.lisa.slinky.models.ErrorResponse
import ceui.lisa.slinky.models.WebApiError
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.ui.dialog.alertNotice
import ceui.lisa.slinky.ui.screenHeight
import ceui.lisa.slinky.utils.NetworkListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import per.goweii.layer.design.cupertino.CupertinoNotificationLayer
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException
import kotlin.math.roundToInt


class ActionItem(val iconRes: Int?, val action: () -> Unit)


class ActionBarViewModel : ViewModel() {

    val title = MutableLiveData<String>()
    val showTitle = MutableLiveData(false)

    val startButtonRes = MutableLiveData(R.drawable.ic_back)
    val startButtonColor = MutableLiveData(0)
    val showStartButton = MutableLiveData(true)

    val endItems = MutableLiveData(
        listOf(
            ActionItem(null) {

            }
        )
    )
}

class MainViewModel : ViewModel() {
    val screenshotState = MutableLiveData<Boolean>()
}

class MainActivity : FullScreenActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (requireLoggedInUserId() == -1L) {
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
            return
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val screenshot = binding.screenshot
        screenshot.pivotX = 0F
        screenshot.pivotY = screenHeight / 2F

        NetworkListener(this).startListening()


//
//        open.setOnClick {
//            screenshot.isVisible = true
//            val bitmap = ImageUtils.view2Bitmap(view)
//            Glide.with(this).load(bitmap).listener(object : RequestListener<Drawable> {
//                override fun onLoadFailed(
//                    e: GlideException?,
//                    model: Any?,
//                    target: Target<Drawable>,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    return false
//                }
//
//                override fun onResourceReady(
//                    resource: Drawable,
//                    model: Any,
//                    target: Target<Drawable>?,
//                    dataSource: DataSource,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    view.isVisible = false
//                    val animator = ObjectAnimator.ofFloat(screenshot, View.ROTATION_Y, 0F, 6F).apply {
//                        duration = 500L
//                        interpolator = OvershootInterpolator(5F)
//                    }
//                    animator.start()
//                    viewModel.screenshotState.value = true
//                    return false
//                }
//            }).into(screenshot)
//        }

//        close.setOnClick {
//            val animator = ObjectAnimator.ofFloat(screenshot, View.ROTATION_Y, 6F, 0F).apply {
//                duration = 300L
//                interpolator = AccelerateInterpolator()
//                addListener(object : Animator.AnimatorListener {
//                    override fun onAnimationStart(animation: Animator) {
//
//                    }
//
//                    override fun onAnimationEnd(animation: Animator) {
//                        view.isVisible = true
//                        screenshot.isVisible = false
//                        viewModel.screenshotState.value = false
//                    }
//
//                    override fun onAnimationCancel(animation: Animator) {
//                    }
//
//                    override fun onAnimationRepeat(animation: Animator) {
//                    }
//                })
//            }
//            animator.start()
//        }


        Settings.settingsInstance.observe(this) { localSetting ->
            localSetting?.buildAppBackground()?.render(binding.bgLayout.pageBackground)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (localSetting.backgroundBlurEnabled) {
                    val blurEffect = RenderEffect.createBlurEffect(
                        localSetting.backgroundBlurRadius,
                        localSetting.backgroundBlurRadius,
                        Shader.TileMode.CLAMP
                    )
                    binding.bgLayout.pageBackground.setRenderEffect(blurEffect)
                } else {
                    binding.bgLayout.pageBackground.setRenderEffect(null)
                }
            }
        }
    }
}

fun Fragment.showPush(title: String? = null, body: String? = null) {
    activity?.showPushImpl(title, body)
}

fun FragmentActivity.safeCall(action: () -> Unit) {
    try {
        action.invoke()
    } catch (ex: Exception) {
        handleError(ex)
    }
}

fun Fragment.safeCall(action: () -> Unit): Boolean {
    return try {
        action.invoke()
        true
    } catch (ex: Exception) {
        handleError(ex)
        false
    }
}

fun Fragment.handleError(ex: Exception) {
    if (isDetached) {
        return
    }

    if (activity == null || context == null) {
        return
    }

    requireActivity().handleError(ex)
}


fun FragmentActivity.handleError(ex: Exception) {
    val self = this
    ex.printStackTrace()
    lifecycleScope.launch {
        if (ex is HttpException) {
            val errorBody = ex.response()?.errorBody()?.string() ?: return@launch
            try {
                val errorResponse = Util.gson.fromJson(errorBody, ErrorResponse::class.java)
                alertNotice(message = errorResponse.error?.displayMessage() ?: "Unknown Error")
            } catch (e: Exception) {
                e.printStackTrace()
                val errorResponse = Util.gson.fromJson(errorBody, WebApiError::class.java)
                alertNotice(message = errorResponse.message ?: ex.getHumanReadableMessage(self))
            }
        } else {
            if (ex !is CancellationException) {
                alertNotice(message = ex.message ?: "Unknown Error")
            }
        }
    }
}

fun Throwable.getHumanReadableMessage(context: Context): String {
    return if (this is UnknownHostException || this is SSLHandshakeException || this is TimeoutException || this is SocketTimeoutException) {
        "${context.getString(R.string.connection_error)}: ${this.javaClass.simpleName}"
    } else {
        val lc = localizedMessage
        if (lc == null) {
            context.getString(R.string.unknown_error_message)
        } else if (lc.contains("<html") || lc.contains("<!DOCTYPE html")) {
            val titleAfter = lc.substringAfter("<title>")
            val title = titleAfter.substringBefore("</title>")
            title
        } else {
            lc
        }
    }
}

fun FragmentActivity.showPushImpl(title: String?, body: String?) {
    CupertinoNotificationLayer(this)
        .setContentBlurSimple(2F)
        .setLabel(R.string.app_name)
        .setTitle(title ?: "")
        .setDesc(body ?: "")
        .setTimePattern("MM-dd HH:mm")
        .setOnNotificationClickListener { layer, _ -> layer.dismiss() }
        .show()
}

fun requireLoggedInAccountImpl(): AccountResponse {
    return Settings.loggedInAccount
}

fun Context.requireLoggedInAccount(): AccountResponse {
    return requireLoggedInAccountImpl()
}

fun Fragment.requireLoggedInAccount(): AccountResponse {
    return requireActivity().requireLoggedInAccount()
}

fun Fragment.requireLoggedInUserId(): Long {
    return requireActivity().requireLoggedInUserId()
}

fun FragmentActivity.requireLoggedInUserId(): Long {
    return requireLoggedInAccount().user?.id ?: -1L
}

fun Context.dipToPx(dp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
        .roundToInt()
}

fun Fragment.dipToPx(dp: Float): Int {
    return requireActivity().dipToPx(dp)
}

fun Fragment.dipToPxF(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}