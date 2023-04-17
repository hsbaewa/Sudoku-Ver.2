package kr.co.hs.sudoku.extension.platform

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.core.widget.TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
import androidx.core.widget.TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM

object TextViewExtension {

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 텍스트를 뷰 사이즈에 맞게 자동 설정
     **/
    fun TextView.setAutoSizeText() =
        TextViewCompat.setAutoSizeTextTypeWithDefaults(this, AUTO_SIZE_TEXT_TYPE_UNIFORM)

    fun TextView.setAutoSizeTextNone() =
        TextViewCompat.setAutoSizeTextTypeWithDefaults(this, AUTO_SIZE_TEXT_TYPE_NONE)

    fun TextView.isAutoSizeText() =
        TextViewCompat.getAutoSizeTextType(this) == AUTO_SIZE_TEXT_TYPE_UNIFORM


    fun TextView.requestFocusWithKeyboard() {
        requestFocus()
        showSoftKeyboard()
    }

    private fun View.showSoftKeyboard() =
        getInputMethodManager().showSoftInput(this, SHOW_IMPLICIT)

    private fun View.getInputMethodManager() =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    fun TextView.clearFocusWithKeyboard() {
        clearFocus()
        hideSoftKeyboard()
    }

    private fun View.hideSoftKeyboard() =
        getInputMethodManager().hideSoftInputFromWindow(windowToken, HIDE_IMPLICIT_ONLY)
}