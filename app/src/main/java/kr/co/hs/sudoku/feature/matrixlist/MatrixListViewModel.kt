package kr.co.hs.sudoku.feature.matrixlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import kr.co.hs.sudoku.viewmodel.ViewModel

class MatrixListViewModel : ViewModel() {
    private val _matrixList = MutableLiveData<List<IntMatrix>>()
    val matrixList: LiveData<List<IntMatrix>> by this::_matrixList

    private val _selection = MutableLiveData<IntMatrix>()
    val selection: LiveData<IntMatrix> by this::_selection

    fun requestAllMatrix() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val resultList = buildList {
            withContext(Dispatchers.IO) {
                BeginnerMatrixRepository().getList().also { addAll(it) }
                IntermediateMatrixRepository().getList().also { addAll(it) }
                AdvancedMatrixRepository().getList().also { addAll(it) }
            }
        }
        _matrixList.value = resultList.sortedBy { it.boxCount }
        setProgress(false)
    }

    fun select(matrix: IntMatrix) {
        _selection.value = matrix
    }

    fun requestBeginnerMatrix() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val resultList = buildList {
            withContext(Dispatchers.IO) {
                BeginnerMatrixRepository().getList().also { addAll(it) }
            }
        }
        _matrixList.value = resultList.sortedBy { it.boxCount }
        setProgress(false)
    }

    fun requestIntermediateMatrix() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val resultList = buildList {
            withContext(Dispatchers.IO) {
                IntermediateMatrixRepository().getList().also { addAll(it) }
            }
        }
        _matrixList.value = resultList.sortedBy { it.boxCount }
        setProgress(false)
    }

    fun requestAdvancedMatrix() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val resultList = buildList {
            withContext(Dispatchers.IO) {
                AdvancedMatrixRepository().getList().also { addAll(it) }
            }
        }
        _matrixList.value = resultList.sortedBy { it.boxCount }
        setProgress(false)
    }
}