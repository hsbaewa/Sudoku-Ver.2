package kr.co.hs.sudoku.usecase.ranking

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.rank.RankingRepository

class GetRecordUseCaseImpl(
    private val repository: RankingRepository
) : GetRecordUseCase {
    override fun invoke(uid: String) = flow { emit(repository.getRecord(uid)) }
}