package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeReaderRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.record.RecordRepository
import kr.co.hs.sudoku.usecase.challenge.GetChallengeUseCaseImpl
import kr.co.hs.sudoku.usecase.record.GetRecordsUseCaseImpl
import kr.co.hs.sudoku.usecase.record.GetRecordUseCaseImpl
import java.util.Collections

class ChallengeViewModel : ViewModel() {
    private val _top10 = MutableLiveData(getDefaultTop10())
    val top10: LiveData<List<RankerEntity>> by this::_top10

    private val _myRecord = MutableLiveData<RankerEntity?>()
    val myRecord: LiveData<RankerEntity?> by this::_myRecord

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> by this::_error

    private val _isRunningProgress = MutableLiveData(false)
    val isRunningProgress: LiveData<Boolean> by this::_isRunningProgress

    private val _challenge = MutableLiveData<ChallengeEntity>()
    val challenge: LiveData<ChallengeEntity> by this::_challenge

    fun requestChallenge(repository: ChallengeReaderRepository, challengeId: String) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            _isRunningProgress.value = false
            _error.value = throwable
        }) {
            _isRunningProgress.value = true
            val challenge = doRequestChallenge(repository, challengeId)
            _challenge.value = challenge
            _isRunningProgress.value = false
        }
    }

    private suspend fun doRequestChallenge(
        repository: ChallengeReaderRepository,
        challengeId: String
    ) = withContext(Dispatchers.IO) {
        GetChallengeUseCaseImpl(repository).invoke(challengeId).last()
    }

    private suspend fun doRequestLatestChallenge(
        repository: ChallengeReaderRepository
    ) = withContext(Dispatchers.IO) { GetChallengeUseCaseImpl(repository).invoke().last() }

    private suspend fun doRequestLeaderboard(repository: RecordRepository) =
        getDefaultTop10().toMutableList().also { ranking ->
            withContext(Dispatchers.IO) {
                GetRecordsUseCaseImpl(repository).invoke().last()
            }.run {
                Collections.copy(ranking, this)
            }
        }

    private suspend fun doRequestRecord(repository: RecordRepository, uid: String) =
        withContext(Dispatchers.IO) {
            val useCase = GetRecordUseCaseImpl(repository)
            useCase(uid)
                .catch { }
                .lastOrNull()
                ?.takeIf { it.clearTime >= 0 }
        }

    private fun getDefaultTop10() = List(10) {
        RankerEntity("", "-", null, null, null, it.toLong() + 1, -1L)
    }

    fun requestLeaderBoard(
        uid: String?,
        challengeReaderRepository: ChallengeReaderRepository
    ) =
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            _isRunningProgress.value = false
            _error.value = throwable
        }) {
            _isRunningProgress.value = true
            val challenge = doRequestLatestChallenge(challengeReaderRepository)
            val ranking = doRequestLeaderboard(challengeReaderRepository as RecordRepository)

            _challenge.value = challenge
            _top10.value = ranking
            uid.takeIf { it != null }
                ?.let { uid ->
                    ranking.indexOfFirst { it.uid == uid }
                        .takeUnless { it >= 0 }
                        ?.run {
                            doRequestRecord(
                                challengeReaderRepository as RecordRepository,
                                uid
                            )
                        }
                        ?.run { _myRecord.value = this }
                }

            _isRunningProgress.value = false
        }

    fun checkPlaying(challengeRepository: ChallengeRepository) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
        }) {
            _challenge.value?.run {
                if (!isPlaying) {
                    withContext(Dispatchers.IO) { challengeRepository.setPlaying(challengeId) }
                }
            }
        }
    }
}