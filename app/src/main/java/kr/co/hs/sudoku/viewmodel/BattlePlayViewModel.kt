package kr.co.hs.sudoku.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.CellEntity
import kr.co.hs.sudoku.model.stage.CellValueEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.usecase.BuildSudokuUseCaseImpl

class BattlePlayViewModel : ViewModel(), BattleRepository.ParticipantChangedListener,
    BattleRepository.BattleChangedListener {
    companion object {
        private const val TAG = "BattlePlayViewModel"
        fun log(msg: String) = Log.d(TAG, msg)
    }

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> by this::_error

    private val _isRunningProgress = MutableLiveData(false)
    val isRunningProgress: LiveData<Boolean> by this::_isRunningProgress

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isRunningProgress.value = false
        _error.value = throwable
    }

    private val eventMap = HashMap<String, MutableSharedFlow<Event>>()
    private fun emitEvent(participant: BattleParticipantEntity, event: Event) {
        val sharedFlow = eventMap.takeIf { it.containsKey(participant.uid) }
            ?.run { this[participant.uid] }
            ?: kotlin.run {
                MutableSharedFlow<Event>(replay = 10).apply {
                    eventMap[participant.uid] = this
                }
            }
        viewModelScope.launch(coroutineExceptionHandler) { sharedFlow.emit(event) }
    }

    fun getEventFlow(uid: String) = eventMap.takeIf { it.containsKey(uid) }
        ?.run { this[uid] }
        ?: kotlin.run {
            MutableSharedFlow<Event>(replay = 10).apply {
                eventMap[uid] = this
            }
        }

    sealed interface Event {
        val battle: BattleEntity
        val participant: BattleParticipantEntity

        data class OnJoined(
            override val battle: BattleEntity,
            override val participant: BattleParticipantEntity
        ) : Event

        @Suppress("unused")
        data class OnExit(
            override val battle: BattleEntity,
            override val participant: BattleParticipantEntity
        ) : Event

        data class OnReady(
            override val battle: BattleEntity,
            override val participant: BattleParticipantEntity
        ) : Event

        data class OnPrepared(
            override val battle: BattleEntity,
            override val participant: BattleParticipantEntity
        ) : Event

        data class OnStarted(
            override val battle: BattleEntity,
            override val participant: BattleParticipantEntity,
            val stage: Stage
        ) : Event
    }

    fun init(repository: BattleRepository, uid: String) {
        log("init($repository, $uid)")
        this.repository = repository
        this.currentUserId = uid
    }

    private lateinit var repository: BattleRepository
    private lateinit var currentUserId: String

    fun join(battleId: String, profileRepository: ProfileRepository, uid: String) {
        log("join($battleId, $uid)")
        this.battleId = battleId

        viewModelScope.launch(coroutineExceptionHandler) {
            _isRunningProgress.value = true
            val profile = withContext(Dispatchers.IO) { profileRepository.getProfile(uid) }
            withContext(Dispatchers.IO) { repository.joinBattle(battleId, profile) }
            repository.bindBattle(battleId, this@BattlePlayViewModel)
            _isRunningProgress.value = false
        }
    }

    private lateinit var battleId: String

    override fun onChanged(battle: BattleEntity) {
        log("onChanged($battle)")
        val lastStarted = (_battle.value as? BattleEntity.RunningBattleEntity)?.startedAt
        _battle.value = battle

        when (battle) {
            is BattleEntity.PendingBattleEntity -> {
                participantList.value?.forEach { participant ->
                    emitEvent(participant, Event.OnPrepared(battle, participant))
                }
            }

            is BattleEntity.RunningBattleEntity -> {
                if (battle.startedAt != lastStarted) {
                    participantList.value?.forEach { participant ->
                        getStage(participant.uid)?.let { stage ->
                            emitEvent(participant, Event.OnStarted(battle, participant, stage))
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private val _battle = MutableLiveData<BattleEntity>()
    val battle: LiveData<BattleEntity> by this::_battle
    private val observerForBattle = Observer<BattleEntity> { battle ->
        if (participantSize.value != battle.participantSize) {
            log("참여자 정보 변경 ${participantSize.value} -> ${battle.participantSize}")
            _participantSize.value = battle.participantSize
        }

//        if (battle is BattleEntity.RunningBattleEntity) {
//            participantStageMap.forEach {
//                view
//            }
//        }
    }

    private val _participantSize = MutableLiveData<Int>()
    val participantSize: LiveData<Int> by this::_participantSize
    private val observerForParticipantSize = Observer<Int> { doCheckChangedParticipant() }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/05/12
     * @comment 참여자 수가 변경될때 해야 하는 작업 (실제 참여자 조회 및 제거된 참여자 bind 해제)
     **/
    private fun doCheckChangedParticipant() = viewModelScope.launch {
        log("doCheckChangedParticipant() 호출 하여 변경 된 참여자 정보 갱신")
        val participantList = participantList.value ?: emptyList()
        val participantUidList = participantList.map { it.uid }.toSet()
        val remoteParticipantList = withContext(Dispatchers.IO) { requestParticipants() }
        val remoteParticipantUidList = remoteParticipantList.map { it.uid }.toSet()

        // 없어진 참여자 (알고있던 참여자 - 실제 remote에서 가져온 참여자)
        // 에 대해 모니터 중지 및 participantSudoku에서 제거
        participantUidList.subtract(remoteParticipantUidList).forEach { uid ->
            participantList.find { it.uid == uid }?.run {
                log("$displayName 가 참여 인원에서 사라짐")
                repository.unbindParticipant(battleId, uid)
                participantStageMap.remove(uid)
//                emitParticipantEvent(this, ParticipantEvent.OnExit(this))
            }
        }

        // 신규로 추가된 참여자 (실제 remote에서 가져온 참여자 - 알고있던 참여자)
        remoteParticipantUidList.subtract(participantUidList).forEach { uid ->
            remoteParticipantList.find { it.uid == uid }?.run {
                log("$displayName 가 참여 인원에 추가됨")
                val stage = matrix.toStage()
                participantStageMap[uid] = stage
                repository.bindParticipant(battleId, uid, this@BattlePlayViewModel)

                // FIXME: 이거 왜 들어가 있지?...
//                battle.value?.let { battle ->
//                    emitParticipantEvent(
//                        this, ParticipantEvent.OnStarted(
//                            battle, this, stage
//                        )
//                    )
//                }

            }
        }

        _participantList.value = remoteParticipantList
        remoteParticipantList
            .find { it.uid == currentUserId }
            ?.run { _currentProfile.value = this }
    }

    // 알고있는 참여자 리스트
    private val _participantList = MutableLiveData<List<BattleParticipantEntity>>()
    val participantList: LiveData<List<BattleParticipantEntity>> by this::_participantList

    private val _currentProfile = MutableLiveData<BattleParticipantEntity>()
    val currentProfile: LiveData<BattleParticipantEntity> by this::_currentProfile

    // 알고있는 참여자 stage 상태
    private val participantStageMap = HashMap<String, Stage>()

    // 실제 remote로부터 참여자 정보를 얻는다
    private suspend fun requestParticipants() = repository.getParticipantList(battleId)
    private suspend fun IntMatrix.toStage() = BuildSudokuUseCaseImpl(this).invoke().last()

    override fun onChanged(participant: BattleParticipantEntity) {
        log("onChanged($participant)")

        battle.value?.let { battle ->

            log("해당 참여자가 참여함")
            emitEvent(participant, Event.OnJoined(battle, participant))

            if (participant.isReady) {
                // 레디 상태가 변경
                log("해당 참여자가 준비됨")
                // 기존의 알고 있던 stage와 비교하여 해당 참여자의 변경된 셀 조사
                val stage = getStage(participant.uid)
                val changedCellSet = stage?.update(participant.matrix)

                if (changedCellSet?.isNotEmpty() == true) {
                    log("변경된 셀의 갯수가 ${changedCellSet.size} 개 있다")
                }

                when (battle) {
                    is BattleEntity.WaitingBattleEntity -> {
                        log("battle이 대기중인 battle이어서 ready 상태 유지함")
                        emitEvent(participant, Event.OnReady(battle, participant))

                    }

                    is BattleEntity.RunningBattleEntity -> {
                        log("battle이 진행중인 battle이어서 start 상태 유지함")
                        emitEvent(participant, Event.OnReady(battle, participant))

                        stage?.run {
                            emitEvent(participant, Event.OnStarted(battle, participant, stage))
                        }
                    }

                    else -> {}
                }

            }

        }
    }

    private fun Stage.update(from: List<List<Int>>) = buildSet<CellEntity<Int>> {
        from.forEachIndexed { row, rowList ->
            rowList.forEachIndexed { column, value ->
                val cell = getCell(row, column)
                when (cell.value) {
                    CellValueEntity.Empty -> if (value != 0) {
                        cell.toMutable(value)
                        add(cell)
                    }

                    is CellValueEntity.Immutable -> if (value != cell.getValue()) {
                        cell.toImmutable(value)
                        add(cell)
                    }

                    is CellValueEntity.Mutable -> if (value != cell.getValue()) {
                        cell.setValue(value)
                        add(cell)
                    }
                }
            }
        }
    }


    private fun getStage(uid: String) = participantStageMap.takeIf { it.containsKey(uid) }?.get(uid)

    init {
        participantSize.observeForever(observerForParticipantSize)
        battle.observeForever(observerForBattle)
    }

    override fun onCleared() {
        super.onCleared()
        participantSize.removeObserver(observerForParticipantSize)
        battle.removeObserver(observerForBattle)
    }

    fun exit(uid: String, after: () -> Unit) {
        battle.value?.let { battle ->
            viewModelScope.launch(coroutineExceptionHandler) {
                _isRunningProgress.value = true
                withContext(Dispatchers.IO) { repository.exitBattle(battle, uid) }
                after()
                _isRunningProgress.value = false
            }
        }
    }

    fun ready(uid: String) {
        battle.value?.startingMatrix?.let { startingMatrix ->
            val matrix = CustomMatrix(startingMatrix)
            viewModelScope.launch(coroutineExceptionHandler) {
                _isRunningProgress.value = true
                withContext(Dispatchers.IO) {
                    val useCase = AutoGenerateSudokuUseCase(matrix.boxSize, matrix.boxCount, matrix)
                    val stage = useCase().last()
                    repository.updateParticipantMatrix(uid, stage.toValueTable())
                    repository.readyToBattle(uid)
                }
                _isRunningProgress.value = false
            }
        }
    }

    fun releaseReady(uid: String) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isRunningProgress.value = true
            withContext(Dispatchers.IO) { repository.unreadyToBattle(uid) }
            _isRunningProgress.value = false
        }
    }

    fun start(uid: String) {
        battle.value?.let { battle ->
            viewModelScope.launch(coroutineExceptionHandler) {
                _isRunningProgress.value = true
                withContext(Dispatchers.IO) { repository.startBattle(battle, uid) }
                _isRunningProgress.value = false
            }
        }
    }

    fun isMutableCell(row: Int, column: Int): Boolean {
        val result = participantStageMap.takeIf { it.containsKey(currentUserId) }
            ?.run { this[currentUserId] }
            ?.run { !getCell(row, column).isImmutable() }
        return result ?: false
    }

    operator fun set(row: Int, column: Int, value: Int) =
        participantStageMap.takeIf { it.containsKey(currentUserId) }
            ?.run { this[currentUserId] }
            ?.let { stage ->
                stage[row, column] = value
                viewModelScope.launch(coroutineExceptionHandler) {
                    withContext(Dispatchers.IO) {
                        repository.updateParticipantMatrix(currentUserId, stage.toValueTable())
                    }
                }
            }

    fun pending(uid: String) {
        battle.value?.let { battle ->
            val matrix = CustomMatrix(battle.startingMatrix)
            viewModelScope.launch(coroutineExceptionHandler) {
                _isRunningProgress.value = true
                withContext(Dispatchers.IO) {
                    val useCase = AutoGenerateSudokuUseCase(matrix.boxSize, matrix.boxCount, matrix)
                    val stage = useCase().last()
                    repository.updateParticipantMatrix(uid, stage.toValueTable())
                    repository.pendingBattle(battle, uid)
                }
                _isRunningProgress.value = false
            }
        }
    }
}