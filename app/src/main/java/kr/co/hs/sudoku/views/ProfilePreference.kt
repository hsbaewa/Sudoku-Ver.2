package kr.co.hs.sudoku.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.CoilExt.appImageLoader
import kr.co.hs.sudoku.extension.Number.dp
import java.net.URL

class ProfilePreference : Preference {

    companion object {
        const val TAG_ICON_PROGRESS_INDICATOR = "TAG_ICON_PROGRESS_INDICATOR"
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        iconImageView = holder.findViewById(android.R.id.icon) as ImageView
    }

    private lateinit var iconImageView: ImageView


    fun loadIcon(data: URL, errorIcon: Drawable? = null) =
        context.appImageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(data.toString())
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

    private val onStartLoadIcon = { _: Drawable? ->
        val drawable = object : CircularProgressDrawable(context) {
            override fun getIntrinsicWidth() = 30.dp.toInt()
            override fun getIntrinsicHeight() = 30.dp.toInt()
        }
        with(drawable) {
            strokeWidth = 2.dp
            setColorSchemeColors(ContextCompat.getColor(context, R.color.gray_500))
            start()
        }
        icon = drawable
    }

    private val onSuccessLoadIcon: (Drawable) -> Unit = { icon ->
        this.icon = icon
    }

    private val onErrorLoadIcon: (Drawable?) -> Unit = { error ->
        icon = error
    }
}