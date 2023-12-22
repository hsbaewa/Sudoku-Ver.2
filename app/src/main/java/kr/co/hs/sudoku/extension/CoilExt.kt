package kr.co.hs.sudoku.extension

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import kr.co.hs.sudoku.R
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
}