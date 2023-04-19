package kr.co.hs.sudoku.usecase.ranking

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.rank.RankerEntity

interface GetRankingUseCase {
    operator fun invoke(): Flow<List<RankerEntity>>
}