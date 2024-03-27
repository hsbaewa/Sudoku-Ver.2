package kr.co.hs.sudoku.data

import kr.co.hs.sudoku.model.history.HistoryEntity
import java.util.Date
import javax.inject.Inject

class TestHistoryDataSource
@Inject constructor() {

    val dummyData = hashMapOf<String, HistoryEntity>(
        "0" to HistoryEntity.ChallengeClear("0", Date(3), "uid-dummy", "0", 1, Date(3), 2000),
        "1" to HistoryEntity.ChallengeClear("1", Date(5), "uid-dummy", "1", 2, Date(5), 3000)
    )
}