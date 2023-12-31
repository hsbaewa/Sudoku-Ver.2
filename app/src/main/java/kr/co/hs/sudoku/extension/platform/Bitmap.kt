package kr.co.hs.sudoku.extension.platform

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF

object Bitmap {
    fun Bitmap.toCropCircle() = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
        val canvas = Canvas(it)

        canvas.drawRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            width.div(2f),
            height.div(2f),
            Paint().apply { flags = Paint.ANTI_ALIAS_FLAG }
        )

        canvas.drawBitmap(
            this,
            0f,
            0f,
            Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP) }
        )
    }
//    : Bitmap {
//        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(output)
//        val paintColor = Paint().apply {
//            flags = Paint.ANTI_ALIAS_FLAG
//        }
//        val rectF = RectF(Rect(0, 0, width, height))
//        canvas.drawRoundRect(
//            rectF,
//            (width / 2).toFloat(),
//            (height / 2).toFloat(),
//            paintColor
//        )
//        val paintImage = Paint()
//        paintImage.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
//        canvas.drawBitmap(this, 0f, 0f, paintImage)
//        return output
//    }
}