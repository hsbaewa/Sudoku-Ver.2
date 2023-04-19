package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.flow.Flow

interface GetChallengeListUseCase {
    operator fun invoke(): Flow<List<String>>
}