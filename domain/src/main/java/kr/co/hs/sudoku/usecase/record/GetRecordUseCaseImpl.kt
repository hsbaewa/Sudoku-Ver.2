package kr.co.hs.sudoku.usecase.record

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.record.RecordRepository

class GetRecordUseCaseImpl(
    private val repository: RecordRepository
) : GetRecordUseCase {
    override fun invoke(uid: String) = flow { emit(repository.getRecord(uid)) }
}