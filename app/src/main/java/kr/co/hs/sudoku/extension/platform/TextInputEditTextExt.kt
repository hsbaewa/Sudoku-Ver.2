package kr.co.hs.sudoku.extension.platform

import android.view.View
import androidx.annotation.IntDef
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

object TextInputEditTextExt {
    private val TextInputEditText.parentLayout: TextInputLayout?
        get() = parent.parent as? TextInputLayout

    @IntDef(View.VISIBLE, View.INVISIBLE, View.GONE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Visibility

    fun TextInputEditText.setVisibilityWithLayout(@Visibility visibility: Int) {
        this.visibility = visibility
        this.parentLayout?.visibility = visibility
    }
}