package kr.co.hs.sudoku.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import coil.request.Disposable
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.CoilExt.loadProfileImage
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity

class ProfileView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        inflate(context, R.layout.layout_view_profile, this)
    }

    private val ivProfileIcon: ImageView by lazy { findViewById(R.id.iv_profile_icon) }
    private val tvFlag: TextView by lazy { findViewById(R.id.tv_flag) }
    private val tvDisplayName: TextView by lazy { findViewById(R.id.tv_display_name) }
    private var disposableProfileIcon: Disposable? = null
    var currentProfile: ProfileEntity? = null

    private fun setProfileIcon(data: String?) = with(ivProfileIcon) {
        disposableProfileIcon = data
            ?.run {
                isVisible = true
                loadProfileImage(this, R.drawable.ic_person)
            }
            ?: run {
                isVisible = false
                setImageDrawable(null)
                null
            }
    }

    private fun setNationFlag(localeEntity: LocaleEntity?) {
        localeEntity?.getLocaleFlag()
            ?.apply { tvFlag.text = this }
            ?: run { tvFlag.isVisible = false }
    }

    private fun setDisplayName(displayName: String?) {
        tvDisplayName.text = displayName ?: ""
    }

    fun setTextColor(colorResId: Int) = with(tvDisplayName) {
        setTextColor(context.getColorCompat(colorResId))
    }

    fun load(profile: ProfileEntity?): Disposable? {
        this.currentProfile = profile
        setProfileIcon(profile?.iconUrl)
        setNationFlag(profile?.locale)
        setDisplayName(profile?.displayName)
        return disposableProfileIcon
    }

    fun clear() {
        ivProfileIcon.setImageDrawable(null)
        ivProfileIcon.isVisible = false
        tvFlag.text = ""
        tvFlag.isVisible = false
        tvDisplayName.text = context.getString(R.string.empty)
    }

    fun cancel() {
        disposableProfileIcon?.dispose()
    }
}