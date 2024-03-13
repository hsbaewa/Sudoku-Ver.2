package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.repository.SudokuStageRandomGenerator
import kr.co.hs.sudoku.core.Stage
import kr.co.hs.sudoku.usecase.abs.UseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SudokuRandomGenerateUseCase
@Inject constructor() : UseCase<List<List<Int>>, Stage, Nothing>() {

    data class Param(val rowSize: Int, val level: Double) : List<List<Int>> by ArrayList()

    override fun invoke(
        param: List<List<Int>>,
        scope: CoroutineScope,
        onResult: (Result<Stage, Nothing>) -> Unit
    ) = scope.launch(Dispatchers.Default) {
        val p = param as Param
        SudokuStageRandomGenerator(p.rowSize, p.level)
            .flow
            .catch { onResult(Result.Exception(it)) }
            .collect { onResult(Result.Success(it)) }
    }

    override suspend fun invoke(param: List<List<Int>>, scope: CoroutineScope): Stage {
        val p = param as Param
        return scope.async { SudokuStageRandomGenerator(p.rowSize, p.level).build(scope) }.await()
    }
}