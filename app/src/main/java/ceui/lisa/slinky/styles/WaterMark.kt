package ceui.lisa.slinky.styles

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.StaticLayout
import android.text.TextPaint

class WaterMark(private val markText: String) : Drawable() {

    private val paint = Paint()
    private val textSize = 80
    private val rotate = -35f

    init {
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.GRAY
        paint.textSize = textSize.toFloat()
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()
        canvas.clipRect(0, 0, width, height)
        canvas.rotate(rotate)
        val cellWidth = StaticLayout.getDesiredWidth(markText, TextPaint(paint)).toInt()
        val xGap = cellWidth + 200
        val yGap = (textSize * 2.5).toInt()
        var flag = true
        for (y in textSize..height * 2 step yGap) {
            val switchGap = if (flag) {
                flag = false
                (cellWidth / 3.5).toInt()
            } else {
                flag = true
                0
            }
            for (x in -width..width step xGap) {
                canvas.drawText(markText, x.toFloat() + switchGap, y.toFloat(), paint)
            }
        }
        canvas.rotate(-rotate)

    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}