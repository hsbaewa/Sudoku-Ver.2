package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.stage.MatrixRepository

class SinglePlayDifficultyViewModel : ViewModel() {

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment Sudoku Matrix 정보, 이를 통해 Stage를 빌드 할 수 있다.
     * @return
     **/
    private val _matrixList = MutableLiveData<List<IntMatrix>>()
    val matrixList: LiveData<List<IntMatrix>> by this::_matrixList

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/10
     * @comment Sudoku Matrix 정보 로드
     **/
    fun requestMatrix(repository: MatrixRepository<IntMatrix>) = viewModelScope.launch {
        val list = withContext(Dispatchers.IO) { repository.getList() }
        _matrixList.value = list
    }
}