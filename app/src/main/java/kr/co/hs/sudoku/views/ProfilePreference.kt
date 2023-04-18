package kr.co.hs.sudoku.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.android.material.progressindicator.CircularProgressIndicator
import kr.co.hs.sudoku.extension.CoilExt.appImageLoader
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
        icon = null
        showIconProgressIndicator()
    }

    private val onSuccessLoadIcon: (Drawable) -> Unit = { icon ->
        this.icon = icon
        dismissIconProgressIndicator()
    }

    private val onErrorLoadIcon: (Drawable?) -> Unit = { error ->
        icon = error
        dismissIconProgressIndicator()
    }

    private fun showIconProgressIndicator() =
        with(iconImageView.parent as ViewGroup) {
            if (findProgressIndicator() == null) {
                createProgressIndicator()
            }
        }

    private fun ViewGroup.findProgressIndicator() =
        findViewWithTag<CircularProgressIndicator>(TAG_ICON_PROGRESS_INDICATOR)

    private fun ViewGroup.createProgressIndicator() =
        CircularProgressIndicator(context).apply {
            isIndeterminate = true
            tag = TAG_ICON_PROGRESS_INDICATOR
        }.also {
            addView(it)
        }

    private fun dismissIconProgressIndicator() =
        with(iconImageView.parent as ViewGroup) { removeProgressIndicator() }

    private fun ViewGroup.removeProgressIndicator() =
        findProgressIndicator()
            .takeIf { it != null }
            ?.let { removeView(it) }
}