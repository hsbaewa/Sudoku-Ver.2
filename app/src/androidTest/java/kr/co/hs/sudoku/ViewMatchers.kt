package kr.co.hs.sudoku

import android.view.View
import kr.co.hs.sudoku.views.SudokuView
import org.hamcrest.Description
import org.hamcrest.Matcher

object ViewMatchers {
    fun <T : View> checkCellValue(row: Int, column: Int, value: Int): Matcher<T> {
        return object : Matcher<T> {
            override fun describeTo(description: Description?) {}
            override fun describeMismatch(item: Any?, mismatchDescription: Description?) {}

            @Deprecated("Deprecated in Java")
            override fun _dont_implement_Matcher___instead_extend_BaseMatcher_() {
            }

            override fun matches(item: Any?) = when (item) {
                is SudokuView -> item.getMatrixValue(row, column) == value
                else -> false
            }
        }
    }
}