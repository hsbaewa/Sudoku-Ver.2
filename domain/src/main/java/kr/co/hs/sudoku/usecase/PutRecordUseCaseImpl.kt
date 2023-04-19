package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.rank.RankingRepository

class PutRecordUseCaseImpl(private val repository: RankingRepository) : PutRecordUseCase {
    override fun invoke(entity: RankerEntity): Flow<Boolean> {
        return flow { emit(repository.putRecord(entity)) }
    }
}