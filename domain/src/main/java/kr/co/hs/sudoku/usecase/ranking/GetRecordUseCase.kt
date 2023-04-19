package kr.co.hs.sudoku.usecase.ranking

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.rank.RankerEntity

interface GetRecordUseCase {
    operator fun invoke(uid: String): Flow<RankerEntity>
}