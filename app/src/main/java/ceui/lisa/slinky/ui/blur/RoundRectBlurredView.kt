package ceui.lisa.slinky.ui.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import ceui.lisa.slinky.R
import ceui.lisa.slinky.ui.pxValue
import ceui.lisa.slinky.utils.DrawableUtils
import com.github.mmin18.widget.RealtimeBlurView

class RoundRectBlurredView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RealtimeBlurView(context, attributeSet) {
    private val mPaint = Paint()
    private val mRectF = RectF()
    private var radius = 20.pxValue.toFloat()
    override fun drawBlurredBitmap(canvas: Canvas?, blurredBitmap: Bitmap?, overlayColor: Int) {
        if (blurredBitmap != null) {
            mRectF.right = width.toFloat()
            mRectF.bottom = height.toFloat()
            mPaint.reset()
            mPaint.isAntiAlias = true
            val shader = BitmapShader(blurredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val matrix = Matrix()
            matrix.postScale(
                mRectF.width() / blurredBitmap.width,
                mRectF.height() / blurredBitmap.height
            )
            shader.setLocalMatrix(matrix)
            mPaint.shader = shader

            (canvas ?: return).drawRect(mRectF, mPaint)

            mPaint.reset()
            mPaint.isAntiAlias = true
            mPaint.color = overlayColor
            DrawableUtils.shoulder(radius, context.getColor(R.color.colorBlack40)).let {
                it.setBounds(0, 0, width, height)
                it.draw(canvas)
            }
        }
    }
}