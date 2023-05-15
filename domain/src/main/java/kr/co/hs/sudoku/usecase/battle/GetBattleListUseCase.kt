package kr.co.hs.sudoku.usecase.battle

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.battle.BattleRepository
import java.util.Date

class GetBattleListUseCase(private val repository: BattleRepository) {
    operator fun invoke(uid: String) = flow { emit(repository.getBattleListCreatedBy(uid)) }
    operator fun invoke(limit: Long) = flow { emit(repository.getBattleList(limit)) }
    operator fun invoke(limit: Long, lastCreateAt: Date) =
        flow { emit(repository.getBattleList(limit, lastCreateAt)) }
}