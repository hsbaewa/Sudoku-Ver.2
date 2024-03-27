package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.impl.ChallengeEntityImpl
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.NoErrorUseCase
import kr.co.hs.sudoku.usecase.SudokuRandomGenerateUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateChallengeUseCase
@Inject constructor(
    private val sudokuGenerator: SudokuRandomGenerateUseCase,
    private val repository: ChallengeRepository
) {

    operator fun invoke(
        matrix: List<List<Int>>
    ): Flow<NoErrorUseCase.Result<ChallengeEntity>> =
        flow {
            val entity: ChallengeEntity = ChallengeEntityImpl(
                challengeId = System.currentTimeMillis().toString(),
                matrix = matrix
            )
            emit(entity)
        }.asUseCaseFlow()

    operator fun invoke(
        size: Int,
        level: Double
    ): Flow<NoErrorUseCase.Result<ChallengeEntity>> =
        flow {
            val stage = coroutineScope {
                sudokuGenerator(
                    SudokuRandomGenerateUseCase.Param(size, level),
                    this
                )
            }.toValueTable()
            emit(stage)
        }.map {
            ChallengeEntityImpl(
                challengeId = System.currentTimeMillis().toString(),
                matrix = it
            )
        }.asUseCaseFlow()

    private fun Flow<ChallengeEntity>.asUseCaseFlow() =
        map {
            repository.createChallenge(it)
        }.flowOn(
            Dispatchers.IO
        ).map {
            @Suppress("USELESS_CAST")
            NoErrorUseCase.Result.Success(it) as NoErrorUseCase.Result<ChallengeEntity>
        }.catch {
            emit(NoErrorUseCase.Result.Exception(it))
        }
}