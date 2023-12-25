package kr.co.hs.sudoku.feature.aiplay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import kr.co.hs.sudoku.feature.single.SinglePlayViewModel
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.viewmodel.ViewModel

class AIPlayViewModel(
    val matrix: IntMatrix
): ViewModel() {
    class ProviderFactory(private val matrix: IntMatrix) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(AIPlayViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                AIPlayViewModel(matrix) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }
}