package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.usecase.battle.GetBattleListUseCase
import kr.co.hs.sudoku.usecase.battle.GetBattleUseCase

class BattleLobbyViewModel : ViewModel() {
    private val _battleList = MutableLiveData<List<BattleEntity>>()
    val battleList: LiveData<List<BattleEntity>> by this::_battleList

    private val _battleDetail = MutableLiveData<BattleEntity>()
    val battleDetail: LiveData<BattleEntity> by this::_battleDetail

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> by this::_error

    private val _isRunningProgress = MutableLiveData(false)
    val isRunningProgress: LiveData<Boolean> by this::_isRunningProgress

    private val _battleCurrent = MutableLiveData<BattleEntity>()
    val battleCurrent: LiveData<BattleEntity> by this::_battleCurrent

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isRunningProgress.value = false
        _error.value = throwable
    }

    fun loadPage(repository: BattleRepository) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isRunningProgress.value = true
            val useCase = GetBattleListUseCase(repository)
            useCase(20).collect { _battleList.value = it }
            _isRunningProgress.value = false
        }
    }

    @Suppress("unused")
    fun loadNextPage(repository: BattleRepository) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isRunningProgress.value = true
            battleList.value
                .takeUnless { it.isNullOrEmpty() }
                ?.let { battleList ->
                    _battleList.value = buildList {
                        addAll(battleList)
                        val useCase = GetBattleListUseCase(repository)
                        addAll(useCase(20, battleList.last().createdAt).last())
                    }
                }
                ?: throw Exception("먼저 첫번째 페이지를 로드해주세요.")
            _isRunningProgress.value = false
        }
    }

    @Suppress("unused")
    fun loadCreatedFrom(repository: BattleRepository, uid: String) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isRunningProgress.value = true
            val useCase = GetBattleListUseCase(repository)
            useCase(uid).collect { _battleList.value = it }
            _isRunningProgress.value = false
        }
    }

    fun loadDetail(repository: BattleRepository, battleId: String) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isRunningProgress.value = true
            val useCase = GetBattleUseCase(repository)
            useCase(battleId).collect {
                _battleDetail.value = it
            }
            _isRunningProgress.value = false
        }
    }

    fun loadCurrent(repository: BattleRepository, uid: String) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isRunningProgress.value = true
            repository.getJoinedBattle(uid)?.let {
                _battleCurrent.value = it
            }
            _isRunningProgress.value = false
        }
    }
}