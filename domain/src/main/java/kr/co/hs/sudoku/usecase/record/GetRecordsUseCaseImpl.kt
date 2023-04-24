package kr.co.hs.sudoku.usecase.record

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.record.RecordRepository

class GetRecordsUseCaseImpl(
    private val repository: RecordRepository
) : GetRecordsUseCase {
    override fun invoke() = flow { emit(repository.getRecords(10)) }
}