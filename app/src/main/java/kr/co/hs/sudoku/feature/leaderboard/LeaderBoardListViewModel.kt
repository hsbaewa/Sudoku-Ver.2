package kr.co.hs.sudoku.feature.leaderboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.viewmodel.ViewModel

class LeaderBoardListViewModel(
    private val battleRepository: BattleRepository? = null,
    private val challengeRepository: ChallengeRepository? = null
) : ViewModel() {
    class ProviderFactory(
        private val battleRepository: BattleRepository? = null,
        private val challengeRepository: ChallengeRepository? = null
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(LeaderBoardListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                LeaderBoardListViewModel(battleRepository, challengeRepository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val _leaderBoardList = MutableLiveData<List<LeaderBoardListItem>>()
    val leaderBoardList: LiveData<List<LeaderBoardListItem>> by this::_leaderBoardList

    private val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    init {
        _leaderBoardList.value = List(10) {
            LeaderBoardListItem.Empty(it.plus(1).toLong())
        }
    }

    fun requestChallengeLeaderBoard(challengeId: String) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)

            val records = withContext(Dispatchers.IO) {
                challengeRepository?.getRecords(challengeId) ?: emptyList()
            }

            val leaderBoardList = MutableList(10) {
                records.runCatching { get(it) }
                    .getOrNull()
                    ?.run { LeaderBoardListItem.ChallengeItem(this, currentUserUid == uid) }
                    ?: LeaderBoardListItem.Empty(it.plus(1).toLong())
            }

            val currentUserUid = this@LeaderBoardListViewModel.currentUserUid
            val hasMyUid = records.find { it.uid == currentUserUid } != null
            if (currentUserUid != null && !hasMyUid) {
                val myRecord = withContext(Dispatchers.IO) {
                    challengeRepository?.runCatching { getRecord(challengeId, currentUserUid) }
                        ?.getOrNull()
                }
                myRecord
                    ?.run { LeaderBoardListItem.ChallengeItemForMine(this) }
                    ?.apply { leaderBoardList.add(this) }
            }

            setProgress(false)
            _leaderBoardList.value = leaderBoardList
        }

    fun requestBattleLeaderBoard() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)

        val records = withContext(Dispatchers.IO) {
            battleRepository?.getLeaderBoard(10) ?: emptyList()
        }

        val leaderBoardList = MutableList(10) {
            records.runCatching { get(it) }
                .getOrNull()
                ?.run { LeaderBoardListItem.BattleItem(this, currentUserUid == uid) }
                ?: LeaderBoardListItem.Empty(it.plus(1).toLong())
        }

        val currentUserUid = this@LeaderBoardListViewModel.currentUserUid
        val hasMyUid = records.find { it.uid == currentUserUid } != null
        if (currentUserUid != null && !hasMyUid) {
            val myRecord = withContext(Dispatchers.IO) {
                battleRepository?.getLeaderBoard(currentUserUid)
            }
            myRecord
                ?.run { LeaderBoardListItem.BattleItemForMine(this) }
                ?.apply { leaderBoardList.add(this) }
        }

        setProgress(false)
        _leaderBoardList.value = leaderBoardList
    }
}