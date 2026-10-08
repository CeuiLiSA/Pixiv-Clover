package ceui.lisa.slinky.utils

import android.graphics.drawable.Drawable
import top.defaults.drawabletoolbox.DrawableBuilder

class DrawableUtils {

    companion object {
        @JvmStatic
        fun simple(corner: Float, solidString: Int): Drawable {
            return DrawableBuilder().rectangle().cornerRadius(corner.toInt())
                .solidColor(solidString).build()
        }

        @JvmStatic
        fun simple(corner: Float, solid: Int, strokeColor: Int, strokeWidth: Float): Drawable {
            return DrawableBuilder().rectangle().cornerRadius(corner.toInt()).solidColor(solid)
                .strokeColor(strokeColor).strokeWidth(strokeWidth.toInt()).build()
        }

        @JvmStatic
        fun stroke(color: Int, width: Float, corner: Float): Drawable {
            return DrawableBuilder().rectangle().cornerRadius(corner.toInt()).strokeColor(color)
                .strokeWidth(width.toInt()).build()
        }

        @JvmStatic
        fun cross(color: Int, corner: Float): Drawable {
            return DrawableBuilder().rectangle().topLeftRadius(corner.toInt())
                .bottomRightRadius(corner.toInt()).solidColor(color).build()
        }

        @JvmStatic
        fun corner(corner: Float): Drawable {
            return DrawableBuilder().rectangle().cornerRadius(corner.toInt()).build()
        }

        @JvmStatic
        fun rectangle(color: Int): Drawable {
            return DrawableBuilder().rectangle().solidColor(color).build()
        }

        fun shoulder(corner: Float, color: Int): Drawable {
            return DrawableBuilder().rectangle().topLeftRadius(corner.toInt())
                .topRightRadius(corner.toInt()).solidColor(color).build()
        }
    }
}