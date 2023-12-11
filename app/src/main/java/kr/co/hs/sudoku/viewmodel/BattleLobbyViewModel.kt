package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository

class BattleLobbyViewModel(
    private val battleRepository: BattleRepository
) : ViewModel() {

    class ProviderFactory(
        private val battleRepository: BattleRepository
    ) : ViewModelProvider.Factory {

        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(BattleLobbyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                BattleLobbyViewModel(battleRepository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val _battleList = MutableLiveData<List<BattleEntity>>()
    val battleList: LiveData<List<BattleEntity>> by this::_battleList

    private val _battleDetail = MutableLiveData<BattleEntity>()
    val battleDetail: LiveData<BattleEntity> by this::_battleDetail

    private val _battleCurrent = MutableLiveData<BattleEntity>()
    val battleCurrent: LiveData<BattleEntity> by this::_battleCurrent

    fun loadPage() {
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            val list = doLoadPage()
            _battleList.value = list

            setProgress(false)
        }
    }

    suspend fun doLoadPage() = withContext(Dispatchers.IO) { battleRepository.list() }

    fun loadDetail(battleId: String) {
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            val entity = withContext(Dispatchers.IO) {
                with(battleRepository) {
                    search(battleId)
                        .apply { getParticipants(this) }
                }
            }
            _battleDetail.value = entity
            setProgress(false)
        }
    }

    fun requestBattleLobby() =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)

            val currentBattle = withContext(Dispatchers.IO) {
                battleRepository
                    .runCatching { getParticipating() }
                    .getOrNull()
                    ?.takeIf {
                        it is BattleEntity.Opened
                                || it is BattleEntity.Pending
                                || it is BattleEntity.Playing
                    }
            }
            currentBattle
                ?.run { _battleCurrent.value = this }
                ?: run {
                    val list = doLoadPage()
                    _battleList.value = list
                }

            setProgress(false)
        }
}