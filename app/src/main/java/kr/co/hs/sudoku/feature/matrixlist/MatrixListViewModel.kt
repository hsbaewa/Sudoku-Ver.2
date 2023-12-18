package kr.co.hs.sudoku.feature.matrixlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.viewmodel.ViewModel

class MatrixListViewModel(
    private val repository: MatrixRepository<IntMatrix>
) : ViewModel() {

    class ProviderFactory(
        private val repository: MatrixRepository<IntMatrix>
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MatrixListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                MatrixListViewModel(repository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val _matrixList = MutableLiveData<List<IntMatrix>>()
    val matrixList: LiveData<List<IntMatrix>> by this::_matrixList

    fun requestList() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val list = withContext(Dispatchers.IO) { repository.getList() }
        _matrixList.value = list
        setProgress(false)
    }
}