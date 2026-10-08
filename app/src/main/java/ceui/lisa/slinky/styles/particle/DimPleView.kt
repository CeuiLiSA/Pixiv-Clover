package ceui.lisa.slinky.styles.particle

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.content.ContextCompat
import ceui.lisa.slinky.R
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.measureTimeMillis

class DimPleView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var mWidth = 0f
    private var mHeight = 0f

    private var particleList = mutableListOf<Particle>()

    private var animator = ValueAnimator.ofFloat(0f, 1f)
    private var paint = Paint()
    private var path = Path()
    private val pathMeasure = PathMeasure()//路径，用于测量扩散圆某一处的X,Y值
    private var pos = FloatArray(2) //扩散圆上某一点的x,y
    private val tan = FloatArray(2)//扩散圆上某一点切线
    private val random = Random()
    private val particleNumber = 1500//粒子数量

    //    private val particleRadius = 2.2f//粒子半径
    private var diffusionRadius = 268f//扩散圆半径
    private var maxOffset = 200F

    fun updateRadius(radius: Float) {
        diffusionRadius = radius
    }

    fun updateMaxOffset(maxOffset: Float) {
        this.maxOffset = maxOffset
    }

    init {
        animator.duration = 2000
        animator.repeatCount = -1
        animator.interpolator = AccelerateInterpolator()
        animator.addUpdateListener {
            updateParticle(it.animatedValue as Float)
            invalidate()
        }
        paint.color = ContextCompat.getColor(getContext(), R.color.purple_500)
        paint.isAntiAlias = true
    }

    private fun updateParticle(fl: Float) {
        particleList.forEachIndexed { index, particle ->
            if (particle.offSet > maxOffset) {
                particle.offSet = 0f
                particle.speed = random.nextInt(3) + 1f
            }
            particle.x =
                (mWidth / 2 + cos(particle.angle) * (diffusionRadius + particle.offSet)).toFloat() + particle.offSetX * particle.direction

            if (particle.y > mHeight / 2) {
                particle.y =
                    (sin(particle.angle) * (diffusionRadius + particle.offSet) + mHeight / 2).toFloat()
            } else {
                particle.y =
                    (mHeight / 2 - sin(particle.angle) * (diffusionRadius + particle.offSet)).toFloat()
            }

            particle.offSet += particle.speed
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val time = measureTimeMillis {
            particleList.forEachIndexed { index, particle ->
                if (particle.offSet > 5f) {
                    val temp = ((1f - particle.offSet / maxOffset) * 255F).toInt()
                    if (temp in 1..255) {
                        paint.alpha = temp
                    } else {
                        paint.alpha = 1
                    }
                    canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
                } else {
                    paint.alpha = 255
                }
                canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
            }
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        path.addCircle(mWidth / 2, mHeight / 2, diffusionRadius, Path.Direction.CCW)
        pathMeasure.setPath(path, false)
        particleList.clear()
        for (i in 0..particleNumber) {
            pathMeasure.getPosTan(i / particleNumber.toFloat() * pathMeasure.length, pos, tan)
            val offSet = random.nextInt(200)
            val speed = random.nextInt(2) + 0.5f
            val randomX = random.nextInt(6) - 3f
            val randomY = random.nextInt(6) - 3f
            val offSetX = random.nextInt(3)
            val direction = random.nextInt(3) - 1.5f
            val particleRadius = random.nextFloat() * 2.2F
            val angel = kotlin.math.acos(((pos[0] - mWidth / 2) / diffusionRadius).toDouble())
            particleList.add(
                Particle(
                    pos[0] + randomX,
                    pos[1] + randomY,
                    particleRadius,
                    offSetX.toFloat(),
                    offSet.toFloat(),
                    direction,
                    speed,
                    angel,
                )
            )
        }
        animator.start()
    }


}

class Particle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var offSetX: Float,
    var offSet: Float,
    var direction: Float,
    var speed: Float,
    var angle: Double,
)