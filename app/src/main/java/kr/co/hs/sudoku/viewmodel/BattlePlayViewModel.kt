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
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.CellEntity
import kr.co.hs.sudoku.model.stage.CellValueEntity
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.usecase.BuildSudokuUseCaseImpl

class BattlePlayViewModel : ViewModel(), BattleRepository.ParticipantChangedListener,
    BattleRepository.BattleChangedListener, IntCoordinateCellEntity.ValueChangedListener {
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

        data class OnChangedCell(
            override val battle: BattleEntity,
            override val participant: BattleParticipantEntity,
            val row: Int, val column: Int, val value: Int?
        ) : Event

        data class OnChangedCellToCorrect(
            override val battle: BattleEntity,
            override val participant: BattleParticipantEntity,
            val set: Set<CellEntity<Int>>
        ) : Event

        data class OnChangedCellToError(
            override val battle: BattleEntity,
            override val participant: BattleParticipantEntity,
            val set: Set<CellEntity<Int>>
        ) : Event

        data class OnCleared(
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
                participantList.value?.forEach { it.onPrepared(battle) }
            }

            is BattleEntity.RunningBattleEntity -> {
                if (battle.startedAt != lastStarted) {
                    participantList.value?.forEach {
                        if (!runningUserIdList.contains(it.uid)) {
                            it.onStarted(battle)
                        }
                    }
                }
            }

            is BattleEntity.ClearedBattleEntity -> {
                if (battle.startedAt != lastStarted) {
                    participantList.value?.forEach {
                        if (!runningUserIdList.contains(it.uid)) {
                            it.onStarted(battle)
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
            log("참여자 숫자 변경 ${participantSize.value} -> ${battle.participantSize}")
            _participantSize.value = battle.participantSize
        }
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
        val exitUserList = participantUidList.subtract(remoteParticipantUidList)
            .mapNotNull { uid -> participantList.find { (it.uid == uid) } }
        onExitParticipantList(exitUserList)

        // 신규로 추가된 참여자 (실제 remote에서 가져온 참여자 - 알고있던 참여자)
        val joinUserList = remoteParticipantUidList.subtract(participantUidList)
            .mapNotNull { uid -> remoteParticipantList.find { it.uid == uid } }
        onJoinParticipantList(joinUserList)

        _participantList.value = remoteParticipantList
        remoteParticipantList
            .find { it.uid == currentUserId }
            ?.run { _currentProfile.value = this }
    }

    private fun onExitParticipantList(list: List<BattleParticipantEntity>) {
        list.forEach {
            log("${it.displayName} 가 참여 인원에서 사라짐")
            repository.unbindParticipant(battleId, it.uid)
            participantStageMap.remove(it.uid)
        }
        list.find { it.uid == currentUserId }?.run {
            repository.unbindBattle(battleId)
        }
    }

    private suspend fun onJoinParticipantList(list: List<BattleParticipantEntity>) {
        val battle = battle.value ?: return
        val matrix = CustomMatrix(battle.startingMatrix)
        list.forEach {
            log("${it.displayName} 가 참여 인원에 추가됨")
            val stage = matrix.toStage()
            if (it.uid == currentUserId) {
                stage.addValueChangedListener(this)
            }
            participantStageMap[it.uid] = stage
            repository.bindParticipant(battleId, it.uid, this)
        }
    }

    // 알고있는 참여자 리스트
    private val _participantList = MutableLiveData<List<BattleParticipantEntity>>()
    val participantList: LiveData<List<BattleParticipantEntity>> by this::_participantList

    private val _currentProfile = MutableLiveData<BattleParticipantEntity>()
    val currentProfile: LiveData<BattleParticipantEntity> by this::_currentProfile

    // 알고있는 참여자 stage 상태
    private val participantStageMap = HashMap<String, Stage>()

    // 마지막으로 알고 있던 실행중인 참여자 id(participant 이벤트에서 해당 참여자가 나가고 들어온 정보를 알아야 해서 추가함. 값을 지워주는 부분은 위의 참여자 사이즈 변환 시점에 한다.)
    private val runningUserIdList = HashSet<String>()

    // 실제 remote로부터 참여자 정보를 얻는다
    private suspend fun requestParticipants() = repository.getParticipantList(battleId)
    private suspend fun IntMatrix.toStage() = BuildSudokuUseCaseImpl(this).invoke().last()
    override fun onChanged(participant: BattleParticipantEntity) {
        log("onChanged($participant)")

        battle.value?.let { battle ->
            if (participant.matrix is EmptyMatrix) {
                participant.onExit(battle)
            } else if (participant.isReady) {
                // 레디 상태가 변경
                // 기존의 알고 있던 stage와 비교하여 해당 참여자의 변경된 셀 조사
                val stage = getStage(participant.uid)
                val changedCellSet = stage?.update(participant.matrix)

                when (battle) {
                    is BattleEntity.WaitingBattleEntity -> participant.onReady(battle)
                    is BattleEntity.RunningBattleEntity,
                    is BattleEntity.ClearedBattleEntity -> {
                        if (runningUserIdList.contains(participant.uid)) {
                            if (changedCellSet?.isNotEmpty() == true && participant.uid != currentUserId) {
                                // 나 자신의 stage 정보는 이미 로컬에서 알고 있기 때문에 변경된 셀이 없는걸로 구분 된다. 그러므로 set 함수에서 자체적으로 이벤트 방출 해야 한다.

                                log("변경된 셀의 갯수가 ${changedCellSet.size} 개 있다")

                                val errorCell = stage.getDuplicatedCells().toList().toHashSet()
                                getLastErrorCell(participant.uid).let { lastErrorCell ->
                                    if (errorCell != lastErrorCell) {
                                        lastErrorCell.subtract(errorCell)
                                            .takeIf { it.isNotEmpty() }
                                            ?.let { participant.onChangedCorrectSet(battle, it) }

                                        errorCell.subtract(lastErrorCell)
                                            .takeIf { it.isNotEmpty() }
                                            ?.let { participant.onChangedErrorSet(battle, it) }

                                        setLastErrorCell(participant.uid, errorCell)
                                    }
                                }


                                changedCellSet
                                    .mapNotNull { it as? IntCoordinateCellEntity }
                                    .forEach {
                                        participant.onChangedCell(battle, it)
                                    }
                                if (stage.isSudokuClear()) {
                                    participant.onCleared(battle, stage)
                                }
                            }
                        } else {
                            // 최초 running 이벤트이므로 시작 이벤트를 방출한다.
                            participant.onStarted(battle)
                        }
                    }

                    else -> {}
                }

            } else {
                participant.onJoined(battle)
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


    fun getStage(uid: String) = participantStageMap.takeIf { it.containsKey(uid) }?.get(uid)

    private var lastErrorCell = HashMap<String, Set<CellEntity<Int>>>()
    private fun getLastErrorCell(uid: String): Set<CellEntity<Int>> {
        return lastErrorCell.takeIf { it.contains(uid) }?.get(uid) ?: emptySet()
    }

    private fun setLastErrorCell(uid: String, set: Set<CellEntity<Int>>) {
        lastErrorCell[uid] = set
    }

    init {
        participantSize.observeForever(observerForParticipantSize)
        battle.observeForever(observerForBattle)
    }

    override fun onCleared() {
        super.onCleared()
        participantSize.removeObserver(observerForParticipantSize)
        battle.removeObserver(observerForBattle)
    }

    private fun BattleParticipantEntity.onJoined(battle: BattleEntity) {
        log("참여자[$displayName]가 참여함")
        emitEvent(this, Event.OnJoined(battle, this))
    }

    private fun BattleParticipantEntity.onReady(battle: BattleEntity) {
        log("참여자[$displayName]가 준비됨")
        emitEvent(this, Event.OnReady(battle, this))
    }

    private fun BattleParticipantEntity.onPrepared(battle: BattleEntity) {
        log("참여자[$displayName]가 시작 준비됨")
        emitEvent(this, Event.OnPrepared(battle, this))
    }

    private fun BattleParticipantEntity.onStarted(battle: BattleEntity) {
        log("참여자[$displayName]가 시작됨")
        emitEvent(this, Event.OnReady(battle, this))
        getStage(uid)?.let { emitEvent(this, Event.OnStarted(battle, this, it)) }
        runningUserIdList.add(uid)
    }

    private fun BattleParticipantEntity.onChangedCorrectSet(
        battle: BattleEntity,
        set: Set<CellEntity<Int>>
    ) {
        log("참여자[$displayName]의 ${set.size}개의 셀이 올바르게 됨")
        emitEvent(this, Event.OnChangedCellToCorrect(battle, this, set))
    }

    private fun BattleParticipantEntity.onChangedErrorSet(
        battle: BattleEntity,
        set: Set<CellEntity<Int>>
    ) {
        log("참여자[$displayName]의 ${set.size}개의 셀이 올바르지 않게 됨")
        emitEvent(this, Event.OnChangedCellToError(battle, this, set))
    }

    private fun BattleParticipantEntity.onChangedCell(
        battle: BattleEntity,
        intCoordinateCellEntity: IntCoordinateCellEntity
    ) {
        log("참여자[$displayName]의 셀(${intCoordinateCellEntity.row}, ${intCoordinateCellEntity.column})이 변경됨")
        val value =
            if (intCoordinateCellEntity.isEmpty()) null else intCoordinateCellEntity.getValue()
        val row = intCoordinateCellEntity.row
        val column = intCoordinateCellEntity.column
        emitEvent(this, Event.OnChangedCell(battle, this, row, column, value))
    }

    private fun BattleParticipantEntity.onCleared(
        battle: BattleEntity,
        stage: Stage
    ) {
        log("참여자[$displayName]의 게임이 클리어 되었다")
        emitEvent(this, Event.OnCleared(battle, this, stage))
    }

    private fun BattleParticipantEntity.onExit(battle: BattleEntity) {
        log("참여자[$uid]가 나감")
        emitEvent(this, Event.OnExit(battle, this))
        runningUserIdList.remove(uid)
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

                val battle = battle.value
                val participant = currentProfile.value

                if (battle != null && participant != null) {
                    participant.onChangedCell(battle, stage.getCell(row, column))

                    if (stage.isSudokuClear()) {
                        participant.onCleared(battle, stage)
                    }
                }

                if (battle != null && participant != null) {
                    val errorCell = stage.getDuplicatedCells().toList().toHashSet()
                    getLastErrorCell(currentUserId).let { lastErrorCell ->
                        if (errorCell != lastErrorCell) {
                            lastErrorCell.subtract(errorCell)
                                .takeIf { it.isNotEmpty() }
                                ?.let { participant.onChangedCorrectSet(battle, it) }

                            errorCell.subtract(lastErrorCell)
                                .takeIf { it.isNotEmpty() }
                                ?.let { participant.onChangedErrorSet(battle, it) }

                            setLastErrorCell(currentUserId, errorCell)
                        }
                    }
                }

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

    override fun onChanged(cell: IntCoordinateCellEntity) {
        participantStageMap.takeIf { it.containsKey(currentUserId) }
            ?.run { this[currentUserId] }
            ?.run {
                if (isSudokuClear() && getClearTime() >= 0) {
                    updateClearRecord(repository, getClearTime())
                }
            }
    }

    private fun updateClearRecord(battleRepository: BattleRepository, clearTime: Long) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isRunningProgress.value = true

            doUpdateClearRecord(battleRepository, clearTime)
            _isClearSudokuRecord.value = clearTime

            _isRunningProgress.value = false
        }
    }

    private suspend fun doUpdateClearRecord(battleRepository: BattleRepository, clearTime: Long) {
        val battle = battle.value ?: throw Exception("unknown battle")
        val profile = currentProfile.value ?: throw Exception("unknown profile")
        withContext(Dispatchers.IO) {
            battleRepository.updateClearRecord(battle, profile, clearTime)
        }
    }

    private val _isClearSudokuRecord = MutableLiveData(-1L)
    val isClearSudokuRecord: LiveData<Long> by this::_isClearSudokuRecord
}