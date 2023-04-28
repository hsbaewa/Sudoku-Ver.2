package kr.co.hs.sudoku.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.request.Disposable
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.CoilExt.appImageLoader
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat

class ProfilePhotoImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var disposable: Disposable? = null

    fun load(data: Any, placeHolder: Drawable? = null, errorIcon: Drawable? = null) {
        val loading = placeHolder ?: CircularProgressDrawable(context).apply {
            strokeWidth = 4f
            centerRadius = 10f
            setColorSchemeColors(context.getColorCompat(R.color.gray_500))
            start()
        }
        disposable = context.appImageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(data)
                .placeholder(loading)
                .error(errorIcon)
                .target(
                    onStart = onStartLoadIcon,
                    onSuccess = onSuccessLoadIcon,
                    onError = onErrorLoadIcon
                )
                .crossfade(true)
                .transformations(CircleCropTransformation())
                .build()
        )

    }

    private val onStartLoadIcon: (Drawable?) -> Unit = { placeHolder: Drawable? ->
        setImageDrawable(placeHolder)
    }

    private val onSuccessLoadIcon: (Drawable) -> Unit = { icon ->
        setImageDrawable(icon)
    }

    private val onErrorLoadIcon: (Drawable?) -> Unit = { error ->
        setImageDrawable(error)
    }

    fun dispose() {
        disposable?.dispose()
    }
}