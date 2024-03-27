package kr.co.hs.sudoku.data

import kr.co.hs.sudoku.model.rank.RankerEntity
import javax.inject.Inject

class TestRecordDataSource
@Inject constructor() {
    val dummyData = hashSetOf<RankerEntity>()
}