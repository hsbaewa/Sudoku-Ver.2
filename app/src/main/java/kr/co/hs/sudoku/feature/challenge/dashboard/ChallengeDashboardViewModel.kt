package kr.co.hs.sudoku.feature.challenge.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.viewmodel.ViewModel

class ChallengeDashboardViewModel(
    private val repository: ChallengeRepository
) : ViewModel() {

    class ProviderFactory(
        private val repository: ChallengeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ChallengeDashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                ChallengeDashboardViewModel(repository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val _dashboardItemList = MutableLiveData<List<ChallengeDashboardListItem>>()
    val dashboardItemList: LiveData<List<ChallengeDashboardListItem>> by this::_dashboardItemList

    private val _challengeList = MutableLiveData<List<ChallengeEntity>>()
    val challengeList: LiveData<List<ChallengeEntity>> by this::_challengeList

    private val _selected = MutableLiveData<ChallengeEntity>()
    val selected: LiveData<ChallengeEntity> by this::_selected

    private val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun initChallengeDashboard() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val challengeList = withContext(Dispatchers.IO) { repository.getChallengeList(50) }
        val lastChallengeId = challengeList.first().challengeId
        val lastChallenge = withContext(Dispatchers.IO) {
            repository.setChallengeId(lastChallengeId)
            repository.getChallengeDetail(lastChallengeId)
        }

        _challengeList.value = challengeList
        _selected.value = lastChallenge
        _dashboardItemList.value = createDashboardList(lastChallenge)
        setProgress(false)
    }

    fun selectChallenge(challenge: ChallengeEntity) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            val selectedChallenge = withContext(Dispatchers.IO) {
                repository.setChallengeId(challenge.challengeId)
                repository.getChallengeDetail(challenge.challengeId)
            }

            _selected.value = selectedChallenge
            _dashboardItemList.value = createDashboardList(selectedChallenge)
            setProgress(false)
        }

    fun refreshChallenge() = _selected.value
        ?.run { selectChallenge(this) }
        ?: initChallengeDashboard()

    private suspend fun createDashboardList(challenge: ChallengeEntity) = buildList {
        add(ChallengeDashboardListItem.TitleItem(challenge.createdAt))
        add(ChallengeDashboardListItem.MatrixHeaderItem)
        add(ChallengeDashboardListItem.MatrixItem(challenge.matrix))
        add(ChallengeDashboardListItem.RankHeaderItem)

        val records = withContext(Dispatchers.IO) { repository.getRecords(10) }
            .run {
                List(10) {
                    it.takeIf { it < this.size }
                        ?.run { get(this) }
                        ?: RankerEntity("", "-", null, null, null, it + 1L, -1)
                }
            }

        addAll(records.map { ChallengeDashboardListItem.RankItem(it) })

        currentUserUid
            ?.takeIf { myUid -> records.find { it.uid == myUid } == null }
            ?.let { myUid -> repository.runCatching { getRecord(myUid) }.getOrNull() }
            ?.takeIf { myRecord -> myRecord.clearTime >= 0 }
            ?.let { myRecord -> add(ChallengeDashboardListItem.MyRankItem(myRecord)) }

        add(ChallengeDashboardListItem.ChallengeStartItem(challenge))
    }

    suspend fun doDeleteRecord() = withContext(Dispatchers.IO) {
        currentUserUid?.let { repository.deleteRecord(it) }
    }
}