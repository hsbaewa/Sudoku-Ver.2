package kr.co.hs.sudoku

class StageTest2 {

    //    var lastEventCell: CellEntity<Int>? = null
//
//    @Test
//    fun testStage() {
//        val jsonString = "{\n" +
//                "\"size\":4,\n" +
//                "\"boxSize\":2,\n" +
//                "\"boxCount\":2,\n" +
//                "\"stage\":[\n" +
//                "[1,1,1,1],\n" +
//                "[1,1,1,1],\n" +
//                "[1,1,1,1],\n" +
//                "[1,1,1,1]\n" +
//                "]\n" +
//                "}\n"
//
////        val json = Gson().fromJson(jsonString, Stage::class.java)
//
//
//        val stageBuilder = StageEntityImpl.StageBuilder(4)
//            .setBoxCount(2)
//            .setBoxSize(2)
//            .setChangedListener(this)
//
//        stageBuilder.updateImmutableValue(0, 1, 2)
//        assertNull(this.lastEventCell)
//
//        val stage = stageBuilder.build()
//        assertNotNull(stage)
//
//        stage[0, 0] = 1
//        assertEquals(stage.getCell(0, 0), lastEventCell)
//        assertThrows(IllegalArgumentException::class.java) {
//            stage[0, 1] = 3
//        }
//        assertEquals(2, stage[0, 1])
//        assertEquals(1, stage[0, 0])
//
//        val pickCell = stage.getCell(1, 2)
//        assertEquals(CellValueEntity.Empty, pickCell.value)
//
//        stage[3, 3] = 3
//        assertEquals(stage.getCell(3, 3), lastEventCell)
//        assertEquals(3, stage.getBoxGroup(1, 1).getValue(1, 1))
//
//        assertEquals(stage[3, 3], stage.getBoxGroup(1, 1).getValue(1, 1))
//
////        stage.boxGroupTable[1][1][1, 1] = 1
////        assertEquals(stage.getCell(3, 3), lastEventCell)
////        assertEquals(1, stage[3, 3])
//
//        stage[2, 3] = 4
//        assertEquals(stage.getCell(2, 3), lastEventCell)
//        assertEquals(4, stage[2, 3])
//        assertEquals(4, stage.getRowGroupForCellBounded(2)[3])
//        assertEquals(4, stage.getColumnGroupForCellBounded(3)[2])
//
//        stage[1, 2] = 1
//        assertEquals(stage.getCell(1, 2), lastEventCell)
//
//        stage[2, 0] = 2
//        assertEquals(stage.getCell(2, 0), lastEventCell)
//        val cellValue = stage.getCell(2, 1).toMutableValue(0)
//        cellValue.setValue(2)
//        stage[2, 2] = 2
//        assertEquals(stage.getCell(2, 2), lastEventCell)
//        stage[2, 3] = 4
//        assertEquals(stage.getCell(2, 3), lastEventCell)
//        val duplicated = stage.getRowGroupForCellBounded(2).getDuplicated()
//        assertEquals(3, duplicated.size)
//
////        assertEquals(0, stage.getCell(0, 0).boxGroup?.x)
////        assertEquals(0, stage.getCell(0, 0).boxGroup?.y)
////        assertEquals(0, stage.getCell(0, 0).rowGroup?.x)
////        assertEquals(0, stage.getCell(0, 0).columnGroup?.y)
////
////        assertEquals(0, stage.getCell(1, 1).boxGroup?.x)
////        assertEquals(0, stage.getCell(1, 1).boxGroup?.y)
////        assertEquals(1, stage.getCell(1, 1).rowGroup?.x)
////        assertEquals(1, stage.getCell(1, 1).columnGroup?.y)
//    }
//
//    @Test
//    fun testCellTable() {
//        val cellTable = MutableCellTableEntityImpl(3)
////        val cell = cellTable[1, 2]
////        assertThrows(CellValueEntity.UndefinedException::class.java) {
////
////        }
//        val firstCell = cellTable.getCell(1, 2)
////        assertEquals(CellValueEntity.Undefined, cellTable[1, 2])
//
////        var mutableCell = cellTable[1, 2]
////        assertEquals(false, mutableCell is MutableCellEntityImpl)
//        assertThrows(IllegalArgumentException::class.java) {
//            cellTable[1, 2] = 1
//        }
//
//        cellTable.updateMutableValue(1, 2, 1)
////        mutableCell = cellTable[1, 2]
////        assertEquals(true, mutableCell is MutableCellEntityImpl)
////        assertEquals(5, cellTable[1, 2])
//        assertEquals(1, cellTable[1, 2])
////        assertTrue(cellTable[1, 2] == 1)
//
//        cellTable[1, 2] = 2
//        assertEquals(2, cellTable[1, 2])
//
//        assertThrows(IllegalArgumentException::class.java) {
//            cellTable[2, 2] = 3
//        }
//
////        cellTable.setValue(1, 2, 3)
////        assertThrows(IllegalArgumentException::class.java) {
////            cellTable[1, 2] = 4
////        }
////        assertEquals(3, cellTable[1, 2])
//
//        assertEquals(2, firstCell.get())
//
//
//    }
//
//    override fun onChanged(cell: CellEntity<Int>) {
//        lastEventCell = cell
//    }
//
//    @Test
//    fun game1() {
//        val stageBuilder = StageEntityImpl.StageBuilder(3)
//            .setBoxCount(1)
//            .setBoxSize(3)
//            .setChangedListener(this)
//
//        val stage = stageBuilder.build()
//        assertNotNull(stage)
//
////        val box = stage.boxGroupTable[0][0]
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(9, stage.getEmptyCells().size)
//
//        stage[0, 0] = 1
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(8, stage.getEmptyCells().size)
//
//        stage[0, 1] = 2
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(7, stage.getEmptyCells().size)
//
//        stage[0, 2] = 2
//        assertEquals(2, stage.getDuplicated().size)
//        assertEquals(6, stage.getEmptyCells().size)
//
//        stage[0, 2] = 9
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(6, stage.getEmptyCells().size)
//
//        assertThrows(IndexOutOfBoundsException::class.java) {
//            stage[0, 3] = 1
//        }
//
//        assertThrows(IllegalArgumentException::class.java) {
//            stage[1, 0] = 0
//        }
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(6, stage.getEmptyCells().size)
//
//        stage[1, 0] = 3
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(5, stage.getEmptyCells().size)
//
//        stage[1, 1] = 6
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(4, stage.getEmptyCells().size)
//
//        stage[1, 2] = 4
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(3, stage.getEmptyCells().size)
//
//        stage[2, 0] = 5
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(2, stage.getEmptyCells().size)
//
//        stage[2, 1] = 7
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(1, stage.getEmptyCells().size)
//
//        stage[2, 2] = 8
//        assertEquals(0, stage.getDuplicated().size)
//        assertEquals(0, stage.getEmptyCells().size)
//        assertEquals(true, stage.isCompleted())
//    }
//
//    @Test
//    fun createRandomGame1() {
//        val stageBuilder = StageEntityImpl.StageBuilder(9)
//            .setBoxCount(3)
//            .setBoxSize(3)
//            .setChangedListener(this)
//
//        stageBuilder.updateImmutableValue(0, 0, 5)
//        stageBuilder.updateImmutableValue(0, 1, 3)
//        stageBuilder.updateImmutableValue(0, 4, 7)
//        stageBuilder.updateImmutableValue(1, 0, 6)
//        stageBuilder.updateImmutableValue(1, 3, 1)
//        stageBuilder.updateImmutableValue(1, 4, 9)
//        stageBuilder.updateImmutableValue(1, 5, 5)
//        stageBuilder.updateImmutableValue(2, 1, 9)
//        stageBuilder.updateImmutableValue(2, 2, 8)
//        stageBuilder.updateImmutableValue(2, 7, 6)
//        stageBuilder.updateImmutableValue(3, 0, 8)
//        stageBuilder.updateImmutableValue(3, 4, 6)
//        stageBuilder.updateImmutableValue(3, 8, 3)
//        stageBuilder.updateImmutableValue(4, 0, 4)
//        stageBuilder.updateImmutableValue(4, 3, 8)
//        stageBuilder.updateImmutableValue(4, 5, 3)
//        stageBuilder.updateImmutableValue(4, 8, 1)
//        stageBuilder.updateImmutableValue(5, 0, 7)
//        stageBuilder.updateImmutableValue(5, 4, 2)
//        stageBuilder.updateImmutableValue(5, 8, 6)
//        stageBuilder.updateImmutableValue(6, 1, 6)
//        stageBuilder.updateImmutableValue(6, 6, 2)
//        stageBuilder.updateImmutableValue(6, 7, 8)
//        stageBuilder.updateImmutableValue(7, 3, 4)
//        stageBuilder.updateImmutableValue(7, 4, 1)
//        stageBuilder.updateImmutableValue(7, 5, 9)
//        stageBuilder.updateImmutableValue(7, 8, 5)
//        stageBuilder.updateImmutableValue(8, 4, 8)
//        stageBuilder.updateImmutableValue(8, 7, 7)
//        stageBuilder.updateImmutableValue(8, 8, 9)
//
//
//        val stage = stageBuilder.build()
//        assertNotNull(stage)
//
//        val max = stage.maxValue()
//        val min = stage.minValue()
//
////        val box = stage.boxGroupTable[0][0]
//        var count = 0
//        do {
//            if (count > 1000) {
////                stage.rowGroup.forEach { it.clearAll() }
////                val valueString = stage.toValueString()
////                println(valueString)
//                count = 0
//            }
//            for (x in 0 until max) {
//                for (y in 0 until max) {
////                do {
////                    stage[x, y] = rand(min, max)
////                } while (box.getDuplicated().isNotEmpty())
////                    stage
//                    try {
//                        val currentValue = stage[x, y]
//                    } catch (e: NotImplementedError) {
//                        val exist = stage.getBoxGroupForCellBounded(x, y).getValueList()
////                        getBoxGroupInCell(x, y).getValueList()
//                        val list = List(max) { i -> i + 1 }
//                            .mapNotNull { if (exist.contains(it)) null else it }
//                        val value = rand(stage.availableValues(x, y))
//                        if (value > 0) {
//                            stage[x, y] = value
//                        } else {
////                            println()
////                            if (stage.getRowValues(x).size > stage.getColumnValues(y).size) {
////                                stage.rowGroup[x].clearAll()
////                            } else if (stage.getColumnValues(y).size > stage.getRowValues(x).size) {
////                                stage.columnGroup[y].clearAll()
////                            } else {
////                                stage.rowGroup[x].clearAll()
////                                stage.columnGroup[y].clearAll()
////                            }
////                            (stage.getBoxGroupInCell(x,y) as BoxGroupEntityImpl).clearAll()
//                        }
//                        if (stage.getDuplicated().isNotEmpty()) {
////                            if (list.size == 2) {
//
////                                print("")
////                            }
////                            if (list.size == 2) {
////                                stage.rowGroup[x].clearAll()
////                                stage.columnGroup[y].clearAll()
////                            }
//                            stage.clearValue(x, y)
//                        }
//                    }
//                }
//            }
//
//            count++
//
////            val valueStr = stage.getBoxGroup(0, 0).toValueString(stage.boxSize)
//            var valueString = stage.toValueString(stage.size)
//            println(valueString)
//            println()
//
////            val list1 = stage.getBoxGroupEmptyCells()
////            val list2 = stage.getRowGroupEmptyCells()
////            val list3 = stage.getColumnGroupEmptyCells()
//            val emptyList = stage.getEmptyCells()
//            emptyList[0].run {
//                val list = stage.availableValues(x, y)
//                val available = (stage.getBoxGroupForCellBounded(
//                    x,
//                    y
//                ) as CalculateEntity<Int, *>).availableValues(x, y)
////                val finded = stage.findCell(available[])
//                available.forEach {
//                    stage.findCell(it)
//                        .filter { it.value is CellValueEntity.Mutable }
//                        .forEach {
//                            stage.clearValue(it.x, it.y)
//                        }
//                }
//
//                valueString = stage.toValueString(stage.size)
//
//                println()
//            }
//            println()
//
//        } while (!stage.isCompleted())
//
//        val valueList = stage.getValueList()
//        val valueString = stage.toValueString(stage.size)
//
//
//
//        assertEquals(true, stage.isCompleted())
//    }
//
//    fun rand(min: Int, max: Int): Int {
//        val random = Random(System.currentTimeMillis())
//        return random.nextUInt().mod(max.toUInt()).toInt() + min
//    }
//
//    fun rand(`in`: List<Int>): Int {
//        return when {
//            `in`.isEmpty() -> -1
//            `in`.size == 1 -> `in`[0]
//            else -> {
//                val random = Random(System.currentTimeMillis())
//                val min = 0
//                val max = `in`.size - 1
//                val idx = random.nextUInt().mod(max.toUInt()).toInt() + min
//                `in`[idx]
//            }
//        }
//    }
//
//    fun GetValueEntity<Int>.toValueString(size: Int): String {
//        return with(StringBuffer()) {
//            val valueList = getValueList()
//            valueList.forEachIndexed { index, i ->
//                if (index != 0 && index % size == 0)
//                    append("\n")
//
//                append(i)
//                append(" ")
//            }
//
//            toString()
//        }
//    }
}