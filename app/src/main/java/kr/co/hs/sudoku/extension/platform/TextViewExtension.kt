package kr.co.hs.sudoku.extension.platform

import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.core.widget.TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM

object TextViewExtension {

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 텍스트를 뷰 사이즈에 맞게 자동 설정
     **/
    fun TextView.setAutoSizeText() =
        TextViewCompat.setAutoSizeTextTypeWithDefaults(this, AUTO_SIZE_TEXT_TYPE_UNIFORM)
}