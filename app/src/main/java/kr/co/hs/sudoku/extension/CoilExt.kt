package kr.co.hs.sudoku.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.Bitmap.toCropCircle
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat

object CoilExt {
    val Context.appImageLoader: ImageLoader
        get() = ImageLoader.Builder(this).build()

    fun ImageView.load(
        data: Any?,
        placeHolder: Drawable? = null,
        errorIcon: Drawable? = null
    ): Disposable {
        val loading = placeHolder ?: CircularProgressDrawable(context).apply {
            strokeWidth = 4f
            centerRadius = 10f
            setColorSchemeColors(context.getColorCompat(R.color.gray_500))
            start()
        }
        return context.appImageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(data)
                .placeholder(loading)
                .error(errorIcon)
                .target(
                    onStart = { d: Drawable? -> setImageDrawable(d) },
                    onSuccess = { icon -> setImageDrawable(icon) },
                    onError = { error -> setImageDrawable(error) }
                )
                .crossfade(true)
                .transformations(CircleCropTransformation())
                .build()
        )

    }

    fun ImageView.load(
        data: Any?,
        placeHolder: Drawable? = null,
        errorIcon: Drawable? = null,
        onComplete: (Throwable?) -> Unit
    ): Disposable {
        val loading = placeHolder ?: CircularProgressDrawable(context).apply {
            strokeWidth = 4f
            centerRadius = 10f
            setColorSchemeColors(context.getColorCompat(R.color.gray_500))
            start()
        }
        return context.appImageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(data)
                .placeholder(loading)
                .error(errorIcon)
                .listener(
                    onStart = { request -> setImageDrawable(request.placeholder) },
                    onSuccess = { _, result ->
                        setImageDrawable(result.drawable)
                        onComplete(null)
                    },
                    onError = { _, result -> onComplete(result.throwable) }
                )
                .crossfade(true)
                .transformations(CircleCropTransformation())
                .build()
        )
    }

    fun ImageView.loadProfileImage(data: String?, errorResId: Int): Disposable =
        context.appImageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(data)
                .error(errorResId)
                .target(
                    onStart = onStartLoadProfileImage,
                    onSuccess = onSuccessLoadProfileImage,
                    onError = onErrorLoadProfileImage
                )
                .crossfade(true)
                .transformations(CircleCropTransformation())
                .build()
        )


    private val ImageView.onStartLoadProfileImage: (Drawable?) -> Unit
        get() = { _ ->
            val drawable = object : CircularProgressDrawable(context) {
                override fun getIntrinsicWidth() = measuredWidth
                override fun getIntrinsicHeight() = measuredHeight
                override fun getStrokeWidth() = 2.dp
                override fun getColorSchemeColors() =
                    intArrayOf(ContextCompat.getColor(context, R.color.gray_500))
            }
            setImageDrawable(drawable)
            drawable.start()
        }

    private val ImageView.onSuccessLoadProfileImage: (Drawable) -> Unit
        get() = { icon ->
            val bitmapIcon = (icon as BitmapDrawable).bitmap.toCropCircle()
            val bitmap =
                Bitmap.createScaledBitmap(bitmapIcon, measuredWidth, measuredHeight, true)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.color = context.getColorCompat(R.color.gray_600)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.dp
            canvas.drawCircle(
                measuredWidth.toFloat().div(2),
                measuredHeight.toFloat().div(2),
                measuredWidth.toFloat().div(2).minus(0.5f.dp),
                paint
            )
            val bitmap2 = BitmapDrawable(resources, bitmap)
            setImageDrawable(bitmap2)
        }

    private val ImageView.onErrorLoadProfileImage: (Drawable?) -> Unit
        get() = { error ->
            val d = object : DrawableWrapper(error) {
                override fun getIntrinsicWidth() = measuredWidth
                override fun getIntrinsicHeight() = measuredHeight
            }
            setImageDrawable(d)
        }
}