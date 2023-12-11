package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.battle2.BattleEntity
import kr.co.hs.sudoku.model.battle2.ParticipantEntity
import kr.co.hs.sudoku.repository.battle2.BattleEventRepository
import kr.co.hs.sudoku.repository.battle2.BattleEventRepositoryImpl
import kr.co.hs.sudoku.repository.battle2.BattleRepository

class BattlePlayViewModel2(
    private val battleRepository: BattleRepository
) : ViewModel() {
    class ProviderFactory(
        private val battleRepository: BattleRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(BattlePlayViewModel2::class.java)) {
                @Suppress("UNCHECKED_CAST")
                BattlePlayViewModel2(battleRepository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }


    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> by this::_error

    private val _isRunningProgress = MutableLiveData(false)
    val isRunningProgress: LiveData<Boolean> by this::_isRunningProgress
    private fun setProgress(progress: Boolean) {
        _isRunningProgress.value = progress
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isRunningProgress.value = false
        _error.value = throwable
    }

    override fun onCleared() {
        super.onCleared()
        stopEventMonitoring()
    }

    suspend fun doJoin(battleId: String) {
        withContext(Dispatchers.IO) { battleRepository.join(battleId) }
        startEventMonitoring(battleId)
    }

    fun toggleReadyOrStart() {
        viewModelScope.launch(coroutineExceptionHandler) {
            setProgress(true)

            if (isHost()) {
                doPending()
            } else {
                if (isReady()) {
                    doUnready()
                } else {
                    doReady()
                }
            }

            setProgress(false)
        }
    }

    suspend fun doReady() {
        withContext(Dispatchers.IO) { battleRepository.ready() }
    }

    suspend fun doUnready() {
        withContext(Dispatchers.IO) { battleRepository.unready() }
    }

    suspend fun doPending() {
        withContext(Dispatchers.IO) { battleRepository.pendingStart() }
    }

    fun start() {
        viewModelScope.launch(coroutineExceptionHandler) {
            setProgress(true)
            doStart()
            setProgress(false)
        }
    }

    suspend fun doStart() {
        withContext(Dispatchers.IO) { battleRepository.start() }
    }

    fun exit() {
        viewModelScope.launch(coroutineExceptionHandler) {
            setProgress(true)
            doExit()
            setProgress(false)
        }
    }

    suspend fun doExit() {
        withContext(Dispatchers.IO) { battleRepository.exit() }
    }


    private val _battleEntity = MutableLiveData<BattleEntity>()
    val battleEntity: LiveData<BattleEntity> by this::_battleEntity

    suspend fun doStartEventMonitoring(battleId: String) {
        battleEventRepository?.stopMonitoring()

        BattleEventRepositoryImpl(battleId)
            .apply { startMonitoring() }
            .also { battleEventRepository = it }
            .also {
                it.battleFlow.collect { entity ->
                    _battleEntity.value = entity
                }
            }
    }

    suspend fun doGetParticipating() = battleRepository.getParticipating()

    private var monitoringJob: Job? = null
    fun startEventMonitoring(battleId: String) {
        stopEventMonitoring()
        monitoringJob =
            viewModelScope.launch(coroutineExceptionHandler) { doStartEventMonitoring(battleId) }
    }

    private var battleEventRepository: BattleEventRepository? = null

    private fun stopEventMonitoring() {
        monitoringJob?.cancel()
        battleEventRepository?.stopMonitoring()
        battleEventRepository = null
    }

    suspend fun doUpdateMatrix(row: Int, column: Int, value: Int) = withContext(Dispatchers.IO) {
        battleRepository.updateMatrix(row, column, value)
    }

    suspend fun doClear(record: Long) = withContext(Dispatchers.IO) {
        battleRepository.clear(record)
    }

    fun clear(record: Long) = viewModelScope.launch(coroutineExceptionHandler) {
        setProgress(true)
        doClear(record)
        setProgress(false)
    }


    private fun isReady() = when (currentParticipant) {
        is ParticipantEntity.Cleared -> false
        is ParticipantEntity.Guest -> false
        is ParticipantEntity.Host -> true
        is ParticipantEntity.Playing -> true
        is ParticipantEntity.ReadyGuest -> true
        null -> false
    }

    fun isHost() = currentParticipant is ParticipantEntity.Host

    private val currentParticipant: ParticipantEntity?
        get() = _battleEntity.value
            ?.participants
            ?.find { it.uid == battleRepository.currentUserUid }

    fun isClosedBattle() = _battleEntity.value is BattleEntity.Closed
}