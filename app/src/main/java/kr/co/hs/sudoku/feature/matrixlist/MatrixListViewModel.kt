package kr.co.hs.sudoku.feature.matrixlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.repositories.AdvancedMatrixRepositoryQualifier
import kr.co.hs.sudoku.di.repositories.BeginnerMatrixRepositoryQualifier
import kr.co.hs.sudoku.di.repositories.IntermediateMatrixRepositoryQualifier
import kr.co.hs.sudoku.model.matrix.AdvancedMatrix
import kr.co.hs.sudoku.model.matrix.BeginnerMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.matrix.IntermediateMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject

@HiltViewModel
class MatrixListViewModel
@Inject constructor(
    @BeginnerMatrixRepositoryQualifier
    val beginnerMatrixRepository: MatrixRepository<BeginnerMatrix>,
    @IntermediateMatrixRepositoryQualifier
    val intermediateMatrixRepository: MatrixRepository<IntermediateMatrix>,
    @AdvancedMatrixRepositoryQualifier
    val advancedMatrixRepository: MatrixRepository<AdvancedMatrix>
) : ViewModel() {
    private val _matrixList = MutableLiveData<List<IntMatrix>>()
    val matrixList: LiveData<List<IntMatrix>> by this::_matrixList

    private val _selection = MutableLiveData<IntMatrix>()
    val selection: LiveData<IntMatrix> by this::_selection

    fun requestAllMatrix() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val resultList = buildList {
            withContext(Dispatchers.IO) {
                beginnerMatrixRepository.getList().also { addAll(it) }
                intermediateMatrixRepository.getList().also { addAll(it) }
                advancedMatrixRepository.getList().also { addAll(it) }
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
                beginnerMatrixRepository.getList().also { addAll(it) }
            }
        }
        _matrixList.value = resultList.sortedBy { it.boxCount }
        setProgress(false)
    }

    fun requestIntermediateMatrix() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val resultList = buildList {
            withContext(Dispatchers.IO) {
                intermediateMatrixRepository.getList().also { addAll(it) }
            }
        }
        _matrixList.value = resultList.sortedBy { it.boxCount }
        setProgress(false)
    }

    fun requestAdvancedMatrix() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val resultList = buildList {
            withContext(Dispatchers.IO) {
                advancedMatrixRepository.getList().also { addAll(it) }
            }
        }
        _matrixList.value = resultList.sortedBy { it.boxCount }
        setProgress(false)
    }

    fun selectAny() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val item = withContext(Dispatchers.IO) {
            beginnerMatrixRepository.getList().firstOrNull()
        }
        item?.run { select(this) }
        setProgress(false)
    }
}