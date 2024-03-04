package kr.co.hs.sudoku.feature.multi.play

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.repositories.BattleRepositoryQualifier
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.battle.BattleEventRepository
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject

@HiltViewModel
class MultiPlayViewModel @Inject constructor(
    @BattleRepositoryQualifier
    private val battleRepository: BattleRepository
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        stopEventMonitoring()
    }

    fun create(matrix: IntMatrix) {
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            val battleEntity = doCreate(matrix)
            _battleEntity.value = battleEntity
            setProgress(false)
        }
    }

    suspend fun doCreate(matrix: IntMatrix) =
        withContext(Dispatchers.IO) { battleRepository.create(matrix) }

    suspend fun doJoin(battleId: String) {
        withContext(Dispatchers.IO) { battleRepository.join(battleId) }
        startEventMonitoring(battleId)
    }

    fun join(battleId: String) = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val eventRepository = withContext(Dispatchers.IO) { battleRepository.join(battleId) }
        stopEventMonitoring()
        monitoringJob = launch(viewModelScopeExceptionHandler) {
            battleEventRepository?.stopMonitoring()
            eventRepository.startMonitoring()
            battleEventRepository = eventRepository
            eventRepository.battleFlow.collect { entity ->
                _battleEntity.value = entity
            }
        }
        setProgress(false)
    }

    suspend fun doJoin(eventRepository: BattleEventRepository) {
        withContext(Dispatchers.IO) { battleRepository.join(eventRepository.battleId) }
        startEventMonitoring(eventRepository)
    }

    fun toggleReadyOrStart() {
        viewModelScope.launch(viewModelScopeExceptionHandler) {
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
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            doStart()
            setProgress(false)
        }
    }

    suspend fun doStart() {
        withContext(Dispatchers.IO) { battleRepository.start() }
    }

    fun exit() {
        viewModelScope.launch(viewModelScopeExceptionHandler) {
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

    private var monitoringJob: Job? = null
    fun startEventMonitoring(battleId: String) {
        stopEventMonitoring()
        monitoringJob = viewModelScope.launch(viewModelScopeExceptionHandler) {
            battleEventRepository?.stopMonitoring()
            val eventRepository = battleRepository.getEventRepository(battleId)
            eventRepository.startMonitoring()
            battleEventRepository = eventRepository
            eventRepository.battleFlow.collect { entity -> _battleEntity.value = entity }
        }
    }

    fun startEventMonitoring(eventRepository: BattleEventRepository) {
        stopEventMonitoring()
        monitoringJob = viewModelScope.launch(viewModelScopeExceptionHandler) {
            battleEventRepository?.stopMonitoring()
            eventRepository
                .apply { startMonitoring() }
                .also { battleEventRepository = it }
                .also {
                    it.battleFlow.collect { entity ->
                        _battleEntity.value = entity
                    }
                }
        }
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

    fun clear(record: Long) = viewModelScope.launch(viewModelScopeExceptionHandler) {
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

    fun kickPlayer(uid: String) = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        withContext(Dispatchers.IO) { battleRepository.kick(uid) }
        setProgress(false)
    }
}