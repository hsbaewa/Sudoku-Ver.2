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
import java.util.Calendar

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

    private suspend fun doRequestLastChallengeList() = withContext(Dispatchers.IO) {
        val latest = repository.getLatestChallenge()
        latest.createdAt?.run {
            val calendar = Calendar.getInstance().apply { time = this@run }
            calendar.add(Calendar.MONTH, -1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            repository.getChallenges(calendar.time)
        } ?: emptyList()
    }

    private suspend fun doRequestDashboard(challenge: ChallengeEntity) =
        withContext(Dispatchers.IO) {
            repository.setChallengeId(challenge.challengeId)
            buildList<ChallengeDashboardListItem> {
                add(ChallengeDashboardListItem.TitleItem(challenge.createdAt))
                add(ChallengeDashboardListItem.MatrixHeaderItem)
                add(ChallengeDashboardListItem.MatrixItem(challenge.matrix))
                add(ChallengeDashboardListItem.RankHeaderItem)

                val records = repository.getRecords(10)
                addAll(
                    List(10) {
                        ChallengeDashboardListItem.RankItem(
                            records
                                .runCatching { get(it) }
                                .getOrDefault(RankerEntity("", "-", null, null, null, it + 1L, -1))
                        )
                    }
                )

                if (find { it is ChallengeDashboardListItem.RankItem && it.rankEntity.uid == currentUserUid } == null) {

                    val myRecord = currentUserUid?.let { uid ->
                        repository.runCatching { getRecord(uid) }.getOrNull()
                    }
                    myRecord
                        ?.takeIf { it.clearTime >= 0 }
                        ?.run { add(ChallengeDashboardListItem.MyRankItem(this)) }
                }

                add(ChallengeDashboardListItem.ChallengeStartItem(challenge))
            }
        }

    fun setDashboard(challenge: ChallengeEntity? = _selected.value) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)

            val entity = challenge ?: run {
                val list = doRequestLastChallengeList()
                _challengeList.value = list
                list.last()
            }

            val dashboard = doRequestDashboard(entity)
            _dashboardItemList.value = dashboard
            _selected.value = entity
            setProgress(false)
        }

    suspend fun doDeleteRecord() = withContext(Dispatchers.IO) {
        currentUserUid?.let { repository.deleteRecord(it) }
    }
}