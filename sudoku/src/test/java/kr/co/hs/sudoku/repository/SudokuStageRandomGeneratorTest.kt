package kr.co.hs.sudoku.repository

import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration

class SudokuStageRandomGeneratorTest {

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        TestCase.assertNotNull(SudokuStageRandomGenerator(4, 10.0).build(this))
        TestCase.assertNotNull(SudokuStageRandomGenerator(4, 20.0).build(this))
        TestCase.assertNotNull(SudokuStageRandomGenerator(4, 50.0).build(this))
        TestCase.assertNotNull(SudokuStageRandomGenerator(4, 70.0).build(this))

        TestCase.assertNotNull(SudokuStageRandomGenerator(9, 10.0).build(this))
        TestCase.assertNotNull(SudokuStageRandomGenerator(9, 20.0).build(this))
        TestCase.assertNotNull(SudokuStageRandomGenerator(9, 50.0).build(this))
        TestCase.assertNotNull(SudokuStageRandomGenerator(9, 70.0).build(this))
    }
}