package kr.co.hs.sudoku.feature.challenge.play

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.history.impl.CachedHistoryQueue
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.viewmodel.ViewModel

class ChallengePlayViewModel(
    private val challengeId: String,
    private val repository: ChallengeRepository
) : ViewModel() {
    class ProviderFactory(
        private val challengeId: String,
        private val repository: ChallengeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ChallengePlayViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                ChallengePlayViewModel(challengeId, repository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val _challengeEntity = MutableLiveData<ChallengeEntity>()
    val challengeEntity: LiveData<ChallengeEntity> by this::_challengeEntity

    private val _command = MutableLiveData<Command>()
    val command: LiveData<Command> by this::_command

    private lateinit var historyQueue: CachedHistoryQueue

    fun requestChallenge() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)

        val challengeEntity =
            withContext(Dispatchers.IO) { repository.getChallengeDetail(challengeId) }
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
                withContext(Dispatchers.IO) { repository.setPlaying(challengeId) }
            }

            _command.value = Started(matrix)
        }
        setProgress(false)
    }

    fun start(lastStatus: List<List<Int>>) = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        _challengeEntity.value?.run {
            if (!isPlaying) {
                withContext(Dispatchers.IO) { repository.setPlaying(challengeId) }
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


    fun setRecord(clearRecord: Long) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            repository.putRecord(clearRecord)
            setProgress(false)
            _command.value = Cleared(clearRecord)
        }
}