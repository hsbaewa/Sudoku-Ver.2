package kr.co.hs.sudoku.feature.challenge.play

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.core.history.impl.CachedHistoryQueue
import kr.co.hs.sudoku.di.repositories.ChallengeRepositoryQualifier
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject

@HiltViewModel
class ChallengePlayViewModel
@Inject constructor(
    @ChallengeRepositoryQualifier
    private val repository: ChallengeRepository
) : ViewModel() {

    private val _challengeEntity = MutableLiveData<ChallengeEntity>()
    val challengeEntity: LiveData<ChallengeEntity> by this::_challengeEntity

    private val _command = MutableLiveData<Command>()
    val command: LiveData<Command> by this::_command

    private lateinit var historyQueue: CachedHistoryQueue

    fun requestChallenge(challengeId: String) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)

            val challengeEntity =
                withContext(Dispatchers.IO) { repository.getChallenge(challengeId) }
            _challengeEntity.value = challengeEntity

            setProgress(false)
        }

    fun initMatrix() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        _challengeEntity.value?.run {
            _command.value = Matrix(matrix)
        }

        setProgress(false)
    }

    fun create() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        _challengeEntity.value?.run {
            _command.value = Created(matrix)
        }
        setProgress(false)
    }

    fun start() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        _challengeEntity.value?.run {
            if (!isPlaying) {
                withContext(Dispatchers.IO) { repository.putReserveRecord(challengeId) }
            }

            _command.value = Started(matrix)
        }
        setProgress(false)
    }

    fun start(lastStatus: List<List<Int>>) = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        _challengeEntity.value?.run {
            if (!isPlaying) {
                withContext(Dispatchers.IO) { repository.putReserveRecord(challengeId) }
            }
            _command.value = Started(lastStatus)
        }
        setProgress(false)
    }

    fun startReplay() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        _command.value = StartReplay
    }

    sealed interface Command
    data class Matrix(val matrix: IntMatrix) : Command
    data class Created(val stage: List<List<Int>>) : Command

    data class Started(val stage: List<List<Int>>) : Command
    object StartReplay : Command

    data class Cleared(val clearRecord: Long) : Command


    fun setRecord(challengeId: String, clearRecord: Long) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            repository.putRecord(challengeId, clearRecord)
            setProgress(false)
            _command.value = Cleared(clearRecord)
        }
}