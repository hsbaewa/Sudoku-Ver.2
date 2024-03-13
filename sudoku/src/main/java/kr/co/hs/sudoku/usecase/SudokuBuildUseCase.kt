package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.SudokuStageBuilder
import kr.co.hs.sudoku.core.Stage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SudokuBuildUseCase
@Inject constructor() : UseCase<List<List<Int>>, Stage, Nothing>() {
    override fun invoke(
        param: List<List<Int>>,
        scope: CoroutineScope,
        onResult: (Result<Stage, Nothing>) -> Unit
    ) = scope.launch(Dispatchers.Default) {
        SudokuStageBuilder(param)
            .flow
            .catch { onResult(Result.Exception(it)) }
            .collect { onResult(Result.Success(it)) }
    }

    override suspend fun invoke(param: List<List<Int>>, scope: CoroutineScope): Stage =
        scope.async { SudokuStageBuilder(param).build(scope) }.await()
}