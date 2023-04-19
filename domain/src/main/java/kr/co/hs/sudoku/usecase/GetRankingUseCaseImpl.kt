package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.rank.RankingRepository

class GetRankingUseCaseImpl(
    private val repository: RankingRepository
) : GetRankingUseCase {
    override fun invoke() = flow {
        emit(repository.getRanking())
    }
}