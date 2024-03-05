package kr.co.hs.sudoku.feature.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.repositories.ChallengeRepositoryQualifier
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.usecase.RandomCreateSudoku
import kr.co.hs.sudoku.viewmodel.ViewModel
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChallengeManageViewModel
@Inject constructor(
    @ChallengeRepositoryQualifier
    private val repository: ChallengeRepository,
) : ViewModel() {

    private val _challengeList = MutableLiveData<List<ChallengeEntity>>()
    val challengeList: LiveData<List<ChallengeEntity>> by this::_challengeList

    private val _generatedSudoku = MutableLiveData<IntMatrix>()
    val generatedSudoku: LiveData<IntMatrix> by this::_generatedSudoku

    fun getChallengeList() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val list = withContext(Dispatchers.IO) { repository.getChallenges(Date(), 50) }
        _challengeList.value = list
        setProgress(false)
    }

    fun deleteChallenge(challengeEntity: ChallengeEntity) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            val isSuccess =
                withContext(Dispatchers.IO) { repository.removeChallenge(challengeEntity.challengeId) }
            if (isSuccess) {
                _challengeList.value = _challengeList.value
                    ?.toMutableList()
                    ?.apply { remove(challengeEntity) }
            }
            setProgress(false)
        }

    fun generateChallengeSudoku() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val matrix = withContext(Dispatchers.IO) {
            RandomCreateSudoku(9, 50.0)
                .getIntMatrix()
                .run { AutoGenerateSudokuUseCase(boxSize, boxCount, this).invoke().last() }
                .run { CustomMatrix(this.toValueTable()) }
        }
        _generatedSudoku.value = matrix
        setProgress(false)
    }

    fun createChallenge(
        onComplete: (ChallengeEntity?) -> Unit
    ) = viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
        viewModelScopeExceptionHandler.handleException(coroutineContext, throwable)
        onComplete(null)
    }) {
        val matrix = _generatedSudoku.value ?: throw Exception("matrix generate first")

        setProgress(true)
        val calendar = Calendar.getInstance()
        val createdAt = calendar.time
        val entity = ChallengeEntityImpl(
            challengeId = createdAt.time.toString(),
            matrix = matrix
        )

        val isSuccess = withContext(Dispatchers.IO) { repository.createChallenge(entity) }
        setProgress(false)
        if (isSuccess) {
            onComplete(withContext(Dispatchers.IO) { repository.getChallenge(entity.challengeId) })
        } else {
            throw Exception("create failed")
        }

    }
}