package kr.co.hs.sudoku.usecase.record

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.record.RecordRepository

class PutRecordUseCaseImpl(private val repository: RecordRepository) : PutRecordUseCase {
    override fun invoke(entity: RankerEntity): Flow<Boolean> {
        return flow { emit(repository.putRecord(entity)) }
    }
}